using System.Text;
using DotNetEnv;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Scalar.AspNetCore;
using UserService.Api.Constants;
using UserService.Api.Data;
using UserService.Api.Entities;
using UserService.Api.Repositories;
using UserService.Api.Repositories.Interfaces;
using UserService.Api.Services;
using UserService.Api.Services.Interfaces;
using UserServiceImplementation = UserService.Api.Services.UserService;

var builder = WebApplication.CreateBuilder(args);

try
{
    // Allow local .env usage without breaking environments where the file does not exist.
    Env.TraversePath().Load();
}
catch
{
    // Ignore missing/invalid .env and keep default configuration providers.
}

builder.Services.AddControllers();
builder.Services.Configure<ApiBehaviorOptions>(options =>
{
    options.InvalidModelStateResponseFactory = context =>
        new UnprocessableEntityObjectResult(context.ModelState);
});
builder.Services.AddOpenApi();
builder.Services.AddCors(options =>
{
    options.AddPolicy("FrontendPolicy", policy =>
    {
        policy.AllowAnyOrigin()
            .AllowAnyHeader()
            .AllowAnyMethod();
    });
});

var jwtKey = builder.Configuration["Jwt:Key"]
    ?? throw new InvalidOperationException("JWT key is not configured.");
var signingKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey));

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateIssuerSigningKey = true,
            ValidateLifetime = true,
            ValidIssuer = builder.Configuration["Jwt:Issuer"],
            ValidAudience = builder.Configuration["Jwt:Audience"],
            IssuerSigningKey = signingKey,
            ClockSkew = TimeSpan.Zero
        };
    });

builder.Services.AddAuthorization();

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

builder.Services.AddScoped<IUserRepository, UserRepository>();
builder.Services.AddScoped<IRefreshTokenRepository, RefreshTokenRepository>();
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IUserService, UserServiceImplementation>();
builder.Services.AddScoped<IPasswordService, PasswordService>();
builder.Services.AddScoped<ITokenService, JwtTokenService>();
builder.Services.AddScoped<IGoogleAuthService, GoogleAuthService>();
builder.Services.AddScoped<IEmailSenderService, EmailSenderService>(); 
builder.Services.AddScoped<IEmailVerificationService, EmailVerificationService>();
builder.Services.AddScoped<IPasswordResetService, PasswordResetService>();

var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    var dbContext = services.GetRequiredService<AppDbContext>();
    var passwordService = services.GetRequiredService<IPasswordService>();

    if (dbContext.Database.GetMigrations().Any())
    {
        dbContext.Database.Migrate();
    }
    else
    {
        dbContext.Database.EnsureCreated();
    }

    await EnsureAdminUserAsync(dbContext, builder.Configuration, passwordService);
}

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
    app.MapScalarApiReference();
}

app.UseCors("FrontendPolicy");
app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();
app.MapGet("/health", () => Results.Ok(new { status = "healthy", service = "auth-service" }))
   .AllowAnonymous();


app.Run();

static async Task EnsureAdminUserAsync(
    AppDbContext dbContext,
    IConfiguration configuration,
    IPasswordService passwordService)
{
    var adminEmail = configuration["Auth:Email"] ?? "admin@sirmaroc.com";
    var adminPassword = configuration["Auth:Password"] ?? "admin123";
    var adminRole = configuration["Auth:Role"] ?? UserRoles.Admin;
    var now = DateTime.UtcNow;

    var adminUser = await dbContext.Users.FirstOrDefaultAsync(user => user.Email == adminEmail);

    if (adminUser is null)
    {
        dbContext.Users.Add(new User
        {
            FirstName = "Admin",
            LastName = "User",
            Email = adminEmail,
            Password = passwordService.HashPassword(adminPassword),
            Role = adminRole,
            EmailVerified = true,
            CreatedAt = now,
            UpdatedAt = now
        });

        await dbContext.SaveChangesAsync();
        return;
    }

    var hasChanges = false;

    if (!adminUser.EmailVerified)
    {
        adminUser.EmailVerified = true;
        hasChanges = true;
    }

    if (string.IsNullOrWhiteSpace(adminUser.Role))
    {
        adminUser.Role = adminRole;
        hasChanges = true;
    }

    if (string.IsNullOrWhiteSpace(adminUser.Password))
    {
        adminUser.Password = passwordService.HashPassword(adminPassword);
        hasChanges = true;
    }

    if (!hasChanges)
    {
        return;
    }

    adminUser.UpdatedAt = now;
    await dbContext.SaveChangesAsync();
}
