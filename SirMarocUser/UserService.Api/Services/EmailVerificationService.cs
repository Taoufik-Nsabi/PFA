using UserService.Api.Repositories.Interfaces;
using UserService.Api.Services.Interfaces;

namespace UserService.Api.Services;

public class EmailVerificationService : IEmailVerificationService
{
    private readonly IUserRepository _userRepository;
    private readonly IEmailSenderService _emailSenderService;
    private readonly IConfiguration _configuration;

    public EmailVerificationService(
        IUserRepository userRepository,
        IEmailSenderService emailSenderService,
        IConfiguration configuration)
    {
        _userRepository = userRepository;
        _emailSenderService = emailSenderService;
        _configuration = configuration;
    }

    public async Task SendVerificationEmailAsync(string email, string firstName)
    {
        var user = await _userRepository.GetByEmailAsync(email);
        if (user is null)
            throw new InvalidOperationException("User introuvable.");

        // ✅ Générer token unique
        var token = Guid.NewGuid().ToString("N");

        // ✅ Sauvegarder token + expiry en DB (7j)
        user.EmailVerificationToken = token;
        user.EmailVerificationTokenExpiry = DateTime.UtcNow.AddDays(7);
        await _userRepository.UpdateAsync(user);

        // ✅ Construire le lien
        var backendUrl = Environment.GetEnvironmentVariable("BACKEND_URL")
                         ?? _configuration["App:BackendUrl"]
                         ?? "http://localhost:5000";

        var verificationLink = $"{backendUrl}/api/auth/verify-email?token={token}";
        // ✅ Envoyer l'email
        var subject = "Vérifiez votre adresse email — SirMaroc";
        var message = $@"
            Bonjour {firstName},
            Merci de vous être inscrit sur SirMaroc.
            Cliquez sur le lien ci-dessous pour vérifier votre email :
            {verificationLink}
            Ce lien expire dans 7 jours.
            Si vous n'avez pas créé de compte, ignorez cet email.
        ";

        await _emailSenderService.SendMailAsync(email, firstName, subject, message);
    }

    public async Task<bool> VerifyEmailAsync(string token)
    {
        var user = await _userRepository.GetByVerificationTokenAsync(token);

        if (user is null)
            return false;

        if (user.EmailVerificationTokenExpiry < DateTime.UtcNow)
            return false;

        // ✅ Marquer email comme vérifié
        user.EmailVerified = true;
        user.EmailVerificationToken = null;
        user.EmailVerificationTokenExpiry = null;
        await _userRepository.UpdateAsync(user);

        return true;
    }
}
