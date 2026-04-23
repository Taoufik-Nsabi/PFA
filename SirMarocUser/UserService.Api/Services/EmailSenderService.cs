using SendGrid;
using SendGrid.Helpers.Mail;
using UserService.Api.Services.Interfaces;

namespace UserService.Api.Services;

public class EmailSenderService : IEmailSenderService
{
    private readonly IConfiguration _configuration;

    public EmailSenderService(IConfiguration configuration)
    {
        _configuration = configuration;
    }

    public async Task SendMailAsync(string toEmail, string username, string subject, string message)
    {
        var apiKey = Environment.GetEnvironmentVariable("SENDGRID_API_KEY")
                     ?? _configuration["SendGrid:ApiKey"];

        if (string.IsNullOrWhiteSpace(apiKey))
            throw new Exception("La clé API SendGrid est introuvable dans les variables d'environnement.");

        var fromEmail = Environment.GetEnvironmentVariable("SENDGRID_FROM_EMAIL")
                        ?? _configuration["SendGrid:FromEmail"]
                        ?? "raw2raw22r@gmail.com";
        var fromName = Environment.GetEnvironmentVariable("SENDGRID_FROM_NAME")
                       ?? _configuration["SendGrid:FromName"]
                       ?? "SirMaroc";

        var client = new SendGridClient(apiKey);
        var from = new EmailAddress(fromEmail, fromName);
        var to = new EmailAddress(toEmail, username);

        var plainTextContent = message;
        var htmlContent = $@"
            <h2>{subject}</h2>
            <p>{message}</p>";

        var msg = MailHelper.CreateSingleEmail(from, to, subject, plainTextContent, htmlContent);
        var response = await client.SendEmailAsync(msg);

        if (!response.IsSuccessStatusCode)
        {
            var errorBody = await response.Body.ReadAsStringAsync();
            throw new Exception($"Erreur SendGrid : {response.StatusCode} - {errorBody}");
        }
    }
}
