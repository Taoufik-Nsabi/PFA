package ma.sirmaroc.matchs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.gateway")
@Getter
@Setter
public class GatewaySecurityProperties {

    private boolean enabled = true;
    private String sharedSecret = "change-me-gateway-secret";
    private String gatewayHeaderName = "X-Gateway-Token";
    private String usernameHeaderName = "X-Authenticated-User";
    private String rolesHeaderName = "X-Authenticated-Roles";
}
