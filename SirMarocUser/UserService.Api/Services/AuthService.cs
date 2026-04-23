using System.Data;
using Microsoft.EntityFrameworkCore;
using UserService.Api.Data;
using UserService.Api.DTOs.Auth;
using UserService.Api.Entities;
using UserService.Api.Repositories.Interfaces;
using UserService.Api.Services.Interfaces;

namespace UserService.Api.Services;

public class AuthService : IAuthService
{
    private readonly AppDbContext _dbContext;
    private readonly IUserRepository _userRepository;
    private readonly IRefreshTokenRepository _refreshTokenRepository;
    private readonly IPasswordService _passwordService;
    private readonly ITokenService _tokenService;

    public AuthService(
        AppDbContext dbContext,
        IUserRepository userRepository,
        IRefreshTokenRepository refreshTokenRepository,
        IPasswordService passwordService,
        ITokenService tokenService)
    {
        _dbContext = dbContext;
        _userRepository = userRepository;
        _refreshTokenRepository = refreshTokenRepository;
        _passwordService = passwordService;
        _tokenService = tokenService;
    }

    public async Task<LoginResponse?> LoginAsync(LoginRequest request)
    {
        var user = await _userRepository.GetByEmailAsync(request.Email);

        // user.Password! pour supprimer le warning null reference
        if (user is null || !_passwordService.VerifyPassword(user.Password!, request.Password))
            return null;

        return await CreateTokenResponseAsync(user);
    }

    public async Task<LoginResponse?> RefreshAsync(RefreshTokenRequest request)
    {
        await using var transaction = await _dbContext.Database.BeginTransactionAsync(IsolationLevel.Serializable);

        var tokenHash = _tokenService.HashRefreshToken(request.RefreshToken);
        var storedRefreshToken = await _refreshTokenRepository.GetByTokenAsync(tokenHash);

        if (storedRefreshToken is null
            || storedRefreshToken.RevokedAtUtc is not null
            || storedRefreshToken.ExpiresAtUtc <= DateTime.UtcNow)
            return null;

        storedRefreshToken.RevokedAtUtc = DateTime.UtcNow;
        await _refreshTokenRepository.UpdateAsync(storedRefreshToken);

        var response = await CreateTokenResponseAsync(storedRefreshToken.User);
        await transaction.CommitAsync();
        return response;
    }

    private async Task<LoginResponse> CreateTokenResponseAsync(User user)
    {
        var (accessToken, accessExpiresAtUtc) = _tokenService.GenerateAccessToken(user.Id, user.Email, user.Role);
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
            Token = accessToken,
            ExpiresAtUtc = accessExpiresAtUtc,
            RefreshToken = refreshToken,
            RefreshExpiresAtUtc = refreshExpiresAtUtc,
            Role = user.Role,
            UserId = user.Id,
            Email = user.Email
        };
    }
}
