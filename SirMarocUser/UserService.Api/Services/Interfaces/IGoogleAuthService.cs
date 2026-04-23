using UserService.Api.DTOs.Auth;

namespace UserService.Api.Services.Interfaces;

public interface IGoogleAuthService
{
    Task<LoginResponse> GoogleLoginAsync(string idToken);
}