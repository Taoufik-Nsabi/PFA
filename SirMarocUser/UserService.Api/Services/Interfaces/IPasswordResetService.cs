namespace UserService.Api.Services.Interfaces;

public interface IPasswordResetService
{
    Task SendPasswordResetEmailAsync(string email);
    Task<bool> ValidateResetTokenAsync(string token);
    Task<bool> ResetPasswordAsync(string token, string newPassword);
}
