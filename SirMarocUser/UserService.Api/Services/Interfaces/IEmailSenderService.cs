namespace UserService.Api.Services.Interfaces;

public interface IEmailSenderService
{
    Task SendMailAsync(string toEmail, string username, string subject, string message);
}