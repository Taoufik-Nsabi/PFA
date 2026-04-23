using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using UserService.Api.Entities;
using UserService.Api.Data.Seeds;

namespace UserService.Api.Data.Configurations;

public class UserConfiguration : IEntityTypeConfiguration<User>
{
    public void Configure(EntityTypeBuilder<User> builder)
    {
        builder.ToTable("Users");
        builder.HasIndex(x => x.Email).IsUnique();
        builder.HasData(UserSeedData.AdminUser);
    }
}
