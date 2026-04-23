namespace UserService.Api.Services.Interfaces;

public interface ITokenService
{
    (string Token, DateTime ExpiresAtUtc) GenerateAccessToken(int userId, string username, string role);
    (string Token, DateTime ExpiresAtUtc) GenerateRefreshToken();
    string HashRefreshToken(string refreshToken);
}
