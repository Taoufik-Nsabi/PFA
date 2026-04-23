using UserService.Api.DTOs.Auth;

namespace UserService.Api.Services.Interfaces;

public interface IAuthService
{
    Task<LoginResponse?> LoginAsync(LoginRequest request);
    Task<LoginResponse?> RefreshAsync(RefreshTokenRequest request);
}
