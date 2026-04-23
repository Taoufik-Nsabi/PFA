using UserService.Api.Constants;
using UserService.Api.Entities;

namespace UserService.Api.Data.Seeds;

public static class UserSeedData
{
    public const int AdminUserId = 1;

    public static readonly User AdminUser = new()
    {
        Id = AdminUserId,
        FirstName = "Admin",
        LastName = "User",
        Email = "admin@sirmaroc.com",
        Password = "100000.HLtA7QTRzkQUrYfWGRcW4g==.cGjdJEilrAZPjEh7aKI1TuQ05tcMYeB22bPlclqSgNw=",
        Role = UserRoles.Admin,
        EmailVerified = false,
        CreatedAt = new DateTime(2026, 1, 1, 0, 0, 0, DateTimeKind.Utc),
        UpdatedAt = new DateTime(2026, 1, 1, 0, 0, 0, DateTimeKind.Utc)
    };
}
