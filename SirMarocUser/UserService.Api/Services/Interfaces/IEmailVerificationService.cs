namespace UserService.Api.Services.Interfaces;

public interface IEmailVerificationService
{
    Task SendVerificationEmailAsync(string email, string firstName);
    Task<bool> VerifyEmailAsync(string token);
}