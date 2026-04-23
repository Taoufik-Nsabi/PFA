using UserService.Api.Repositories.Interfaces;
using UserService.Api.Services.Interfaces;

namespace UserService.Api.Services;

public class PasswordResetService : IPasswordResetService
{
    private readonly IUserRepository _userRepository;
    private readonly IEmailSenderService _emailSenderService;
    private readonly IPasswordService _passwordService;
    private readonly IConfiguration _configuration;

    public PasswordResetService(
        IUserRepository userRepository,
        IEmailSenderService emailSenderService,
        IPasswordService passwordService,
        IConfiguration configuration)
    {
        _userRepository = userRepository;
        _emailSenderService = emailSenderService;
        _passwordService = passwordService;
        _configuration = configuration;
    }

    public async Task SendPasswordResetEmailAsync(string email)
    {
        var user = await _userRepository.GetByEmailAsync(email);

        // ✅ Sécurité — on ne révèle pas si l'email existe ou non
        if (user is null || user.IsGoogleAccount)
            return;

        // ✅ Générer token unique
        var token = Guid.NewGuid().ToString("N");

        // ✅ Sauvegarder token + expiry en DB (1 heure)
        user.PasswordResetToken = token;
        user.PasswordResetTokenExpiry = DateTime.UtcNow.AddHours(1);
        await _userRepository.UpdateAsync(user);

        // ✅ Construire le lien
        var backendUrl = Environment.GetEnvironmentVariable("BACKEND_URL")
            ?? _configuration["App:BackendUrl"]
            ?? "http://localhost:5000";
        var resetLink = $"{backendUrl}/api/auth/reset-password?token={token}";

        // ✅ Envoyer l'email
        var subject = "Réinitialisation de votre mot de passe — SirMaroc";
        var message = $@"
            Bonjour {user.FirstName},
            Vous avez demandé la réinitialisation de votre mot de passe.
            Cliquez sur le lien ci-dessous pour choisir un nouveau mot de passe :
            {resetLink}
            Ce lien expire dans 1 heure.
            Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
        ";

        await _emailSenderService.SendMailAsync(email, user.FirstName, subject, message);
    }

    public async Task<bool> ValidateResetTokenAsync(string token)
    {
        var user = await _userRepository.GetByPasswordResetTokenAsync(token);
        if (user is null)
            return false;

        return user.PasswordResetTokenExpiry >= DateTime.UtcNow;
    }

    public async Task<bool> ResetPasswordAsync(string token, string newPassword)
    {
        var user = await _userRepository.GetByPasswordResetTokenAsync(token);

        if (user is null)
            return false;

        // ✅ Vérifier si token expiré
        if (user.PasswordResetTokenExpiry < DateTime.UtcNow)
            return false;

        // ✅ Changer mot de passe + supprimer token
        user.Password = _passwordService.HashPassword(newPassword);
        user.PasswordResetToken = null;
        user.PasswordResetTokenExpiry = null;
        user.UpdatedAt = DateTime.UtcNow;
        await _userRepository.UpdateAsync(user);

        return true;
    }
}
