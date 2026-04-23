using UserService.Api.DTOs.Auth;
using UserService.Api.DTOs.User;

namespace UserService.Api.Services.Interfaces;

public interface IUserService
{
    Task<List<UserResponse>> GetAllAsync();
    Task<UserResponse?> GetByIdAsync(int id);
    Task<UserResponse?> GetByEmailAsync(string email);
    Task<UserResponse> RegisterAsync(RegisterRequest request);
    Task<UserResponse> CreateAsync(CreateUserRequest request);
    Task<UserResponse?> UpdateAsync(int id, UpdateUserRequest request);
    Task<bool> DeleteAsync(int id);
}
