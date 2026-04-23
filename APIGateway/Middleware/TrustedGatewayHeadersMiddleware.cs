using System.Security.Claims;

namespace SirmarocGateway.Middleware;

public sealed class TrustedGatewayHeadersMiddleware
{
    private const string SharedSecretConfigKey = "Gateway:SharedSecret";
    private const string DefaultSharedSecret = "change-me-gateway-secret";

    private readonly RequestDelegate _next;
    private readonly IConfiguration _configuration;

    public TrustedGatewayHeadersMiddleware(
        RequestDelegate next,
        IConfiguration configuration)
    {
        _next = next;
        _configuration = configuration;
    }

    public Task InvokeAsync(HttpContext context)
    {
        if (context.Request.Path.StartsWithSegments("/api"))
        {
            var sharedSecret = _configuration[SharedSecretConfigKey];
            context.Request.Headers["X-Gateway-Token"] =
                string.IsNullOrWhiteSpace(sharedSecret) ? DefaultSharedSecret : sharedSecret;

            if (context.User.Identity?.IsAuthenticated == true)
            {
                var username = ResolveUsername(context.User);
                var roles = ResolveRoles(context.User);

                if (!string.IsNullOrWhiteSpace(username))
                    context.Request.Headers["X-Authenticated-User"] = username;

                if (!string.IsNullOrWhiteSpace(roles))
                    context.Request.Headers["X-Authenticated-Roles"] = roles;
            }
        }

        return _next(context);
    }

    private static string? ResolveUsername(ClaimsPrincipal user)
    {
        return user.FindFirst(ClaimTypes.NameIdentifier)?.Value
            ?? user.FindFirst(ClaimTypes.Name)?.Value
            ?? user.FindFirst("sub")?.Value
            ?? user.FindFirst("preferred_username")?.Value
            ?? user.FindFirst(ClaimTypes.Email)?.Value;
    }

    private static string? ResolveRoles(ClaimsPrincipal user)
    {
        var roles = user.FindAll(ClaimTypes.Role)
            .Select(claim => claim.Value)
            .Where(role => !string.IsNullOrWhiteSpace(role))
            .Select(NormalizeRole)
            .Distinct(StringComparer.Ordinal)
            .ToArray();

        return roles.Length == 0 ? null : string.Join(",", roles);
    }

    private static string NormalizeRole(string role)
    {
        const string rolePrefix = "ROLE_";

        var normalized = role.StartsWith(rolePrefix, StringComparison.OrdinalIgnoreCase)
            ? role[rolePrefix.Length..]
            : role;

        return normalized.Trim().ToUpperInvariant();
    }
}
