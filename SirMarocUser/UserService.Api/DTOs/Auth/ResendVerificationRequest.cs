using System.ComponentModel.DataAnnotations;

namespace UserService.Api.DTOs.Auth;

public class ResendVerificationRequest
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;
}