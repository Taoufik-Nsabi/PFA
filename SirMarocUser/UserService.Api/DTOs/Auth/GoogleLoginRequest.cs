using System.ComponentModel.DataAnnotations;

namespace UserService.Api.DTOs.Auth;

public class GoogleLoginRequest
{
    [Required]
    public string IdToken { get; set; } = string.Empty;
}
