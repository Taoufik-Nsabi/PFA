namespace UserService.Api.Constants;

public static class UserRoles
{
    public const string Admin = "Admin";
    public const string User = "User";
    public const string AdminOrUser = $"{Admin},{User}";
    public const string ValidationPattern = $"{Admin}|{User}";

    public static string Normalize(string? role)
    {
        return string.Equals(role, Admin, StringComparison.OrdinalIgnoreCase)
            ? Admin
            : User;
    }
}
