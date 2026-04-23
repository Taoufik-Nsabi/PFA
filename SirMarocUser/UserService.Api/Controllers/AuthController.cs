using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using UserService.Api.DTOs.Auth;
using UserService.Api.Repositories.Interfaces;
using UserService.Api.Services.Interfaces;

namespace UserService.Api.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;
    private readonly IUserRepository _userRepository;
    private readonly IPasswordService _passwordService;
    private readonly IGoogleAuthService _googleAuthService;
    private readonly IEmailVerificationService _emailVerificationService;
    private readonly IPasswordResetService _passwordResetService;
    private readonly IUserService _userService;

    public AuthController(
        IAuthService authService,
        IUserRepository userRepository,
        IPasswordService passwordService,
        IGoogleAuthService googleAuthService,
        IEmailVerificationService emailVerificationService,
        IPasswordResetService passwordResetService,
        IUserService userService)
    {
        _authService = authService;
        _userRepository = userRepository;
        _passwordService = passwordService;
        _googleAuthService = googleAuthService;
        _emailVerificationService = emailVerificationService;
        _passwordResetService = passwordResetService;
        _userService = userService;
    }

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<ActionResult<LoginResponse>> Login([FromBody] LoginRequest request)
    {
        var user = await _userRepository.GetByEmailAsync(request.Email);

        if (user is null)
            return Unauthorized(new { message = "Email ou mot de passe incorrect." });

        if (user.IsGoogleAccount)
            return Unauthorized(new { message = "Ce compte utilise Google. Connectez-vous avec Google." });

        if (!user.EmailVerified)
            return Unauthorized(new { message = "Veuillez verifier votre email avant de vous connecter." });

        if (!_passwordService.VerifyPassword(user.Password!, request.Password))
            return Unauthorized(new { message = "Email ou mot de passe incorrect." });

        var response = await _authService.LoginAsync(request);
        if (response is null)
            return Unauthorized(new { message = "Email ou mot de passe incorrect." });

        response.UserId = user.Id;
        response.Email = user.Email;
        return Ok(response);
    }

    [HttpPost("refresh")]
    [AllowAnonymous]
    public async Task<ActionResult<LoginResponse>> Refresh([FromBody] RefreshTokenRequest request)
    {
        var response = await _authService.RefreshAsync(request);
        if (response is null)
            return Unauthorized(new { message = "Invalid or expired refresh token." });

        return Ok(response);
    }

    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        try
        {
            await _userService.RegisterAsync(request);
            try
            {
                await _emailVerificationService.SendVerificationEmailAsync(request.Email, request.FirstName);
                return Ok(new { message = "Inscription reussie. Verifiez votre email." });
            }
            catch
            {
                return StatusCode(StatusCodes.Status503ServiceUnavailable, new
                {
                    message = "Compte cree, mais l'envoi de l'email de verification a echoue temporairement. Reessayez /api/auth/resend-verification."
                });
            }
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    [HttpGet("verify-email")]
    [AllowAnonymous]
    public async Task<IActionResult> VerifyEmail([FromQuery] string token)
    {
        var result = await _emailVerificationService.VerifyEmailAsync(token);
        if (!result)
            return BadRequest(new { message = "Token invalide ou expire." });

        return Ok(new { message = "Email verifie avec succes. Vous pouvez vous connecter." });
    }

    [HttpPost("resend-verification")]
    [AllowAnonymous]
    public async Task<IActionResult> ResendVerification([FromBody] ResendVerificationRequest request)
    {
        var user = await _userRepository.GetByEmailAsync(request.Email);
        if (user is null || user.EmailVerified)
            return Ok(new { message = "Si cet email existe, un email a ete envoye." });

        try
        {
            await _emailVerificationService.SendVerificationEmailAsync(user.Email, user.FirstName);
            return Ok(new { message = "Si cet email existe, un email a ete envoye." });
        }
        catch
        {
            return StatusCode(StatusCodes.Status503ServiceUnavailable, new
            {
                message = "Service email temporairement indisponible. Reessayez plus tard."
            });
        }
    }

    [HttpPost("forgot-password")]
    [AllowAnonymous]
    public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordRequest request)
    {
        await _passwordResetService.SendPasswordResetEmailAsync(request.Email);
        return Ok(new { message = "Si cet email existe, un email a ete envoye." });
    }

    [HttpGet("reset-password")]
    [AllowAnonymous]
    public async Task<IActionResult> ValidateResetPasswordToken([FromQuery] string token)
    {
        if (string.IsNullOrWhiteSpace(token))
            return BadRequest(new { message = "Token manquant." });

        var isValid = await _passwordResetService.ValidateResetTokenAsync(token);
        if (!isValid)
            return BadRequest(new { message = "Token invalide ou expire." });

        return Ok(new
        {
            message = "Token valide. Utilisez POST /api/auth/reset-password avec le token et le nouveau mot de passe."
        });
    }

    [HttpPost("reset-password")]
    [AllowAnonymous]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordRequest request)
    {
        var result = await _passwordResetService.ResetPasswordAsync(request.Token, request.NewPassword);
        if (!result)
            return BadRequest(new { message = "Token invalide ou expire." });

        return Ok(new { message = "Mot de passe reinitialise avec succes." });
    }

    [HttpPost("google-login")]
    [AllowAnonymous]
    public async Task<IActionResult> GoogleLogin([FromBody] GoogleLoginRequest request)
    {
        try
        {
            var response = await _googleAuthService.GoogleLoginAsync(request.IdToken);
            return Ok(response);
        }
        catch (UnauthorizedAccessException ex)
        {
            return Unauthorized(new { message = ex.Message });
        }
    }
}
