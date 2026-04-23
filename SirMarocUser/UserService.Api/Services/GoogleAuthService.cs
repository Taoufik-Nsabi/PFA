using Google.Apis.Auth;
using UserService.Api.DTOs.Auth;
using UserService.Api.Entities;
using UserService.Api.Repositories.Interfaces;
using UserService.Api.Services.Interfaces;

namespace UserService.Api.Services;

public class GoogleAuthService : IGoogleAuthService
{
    private readonly IUserRepository _userRepository;
    private readonly IRefreshTokenRepository _refreshTokenRepository;
    private readonly ITokenService _tokenService;
    private readonly IConfiguration _configuration;

    public GoogleAuthService(
        IUserRepository userRepository,
        IRefreshTokenRepository refreshTokenRepository,
        ITokenService tokenService,
        IConfiguration configuration)
    {
        _userRepository = userRepository;
        _refreshTokenRepository = refreshTokenRepository;
        _tokenService = tokenService;
        _configuration = configuration;
    }

    public async Task<LoginResponse> GoogleLoginAsync(string idToken)
    {
        var clientId = Environment.GetEnvironmentVariable("GOOGLE_CLIENT_ID")
                       ?? _configuration["GoogleAuth:ClientId"];

        if (string.IsNullOrWhiteSpace(clientId))
            throw new UnauthorizedAccessException("Google Client ID est introuvable.");

        var settings = new GoogleJsonWebSignature.ValidationSettings
        {
            Audience = new[] { clientId }
        };

        GoogleJsonWebSignature.Payload payload;
        try
        {
            payload = await GoogleJsonWebSignature.ValidateAsync(idToken, settings);
        }
        catch
        {
            throw new UnauthorizedAccessException("Token Google invalide");
        }

        var user = await _userRepository.GetByEmailAsync(payload.Email);

        if (user is null)
        {
            user = new User
            {
                Email = payload.Email,
                FirstName = payload.GivenName,
                LastName = payload.FamilyName,
                Password = null,
                IsGoogleAccount = true,
                EmailVerified = true,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };
            await _userRepository.CreateAsync(user);
        }

        var (token, expiresAtUtc) = _tokenService.GenerateAccessToken(user.Id, user.Email, user.Role);
        var (refreshToken, refreshExpiresAtUtc) = _tokenService.GenerateRefreshToken();

        await _refreshTokenRepository.CreateAsync(new RefreshToken
        {
            Id = Guid.NewGuid(),
            Token = _tokenService.HashRefreshToken(refreshToken),
            UserId = user.Id,
            CreatedAtUtc = DateTime.UtcNow,
            ExpiresAtUtc = refreshExpiresAtUtc
        });

        return new LoginResponse
        {
            Token = token,
            ExpiresAtUtc = expiresAtUtc,
            RefreshToken = refreshToken,
            RefreshExpiresAtUtc = refreshExpiresAtUtc,
            Role = user.Role,
            UserId = user.Id,
            Email = user.Email
        };
    }
}
