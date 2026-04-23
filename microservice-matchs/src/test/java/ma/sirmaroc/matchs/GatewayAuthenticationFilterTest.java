package ma.sirmaroc.matchs;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.sirmaroc.matchs.config.GatewayAuthenticationFilter;
import ma.sirmaroc.matchs.config.GatewaySecurityProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayAuthenticationFilterTest {

    private static final String GATEWAY_SECRET = "gateway-secret";

    private GatewayAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        properties.setEnabled(true);
        properties.setSharedSecret(GATEWAY_SECRET);
        filter = new GatewayAuthenticationFilter(properties, new ObjectMapper().findAndRegisterModules());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldBlockDirectApiRequestsWithoutTrustedGatewayHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/matchs");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("API Gateway");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void shouldAuthenticateUsingGatewayHeadersWhenGatewaySecretIsValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/sync/api-football/fixtures");
        request.addHeader("X-Gateway-Token", GATEWAY_SECRET);
        request.addHeader("X-Authenticated-User", "gateway-admin");
        request.addHeader("X-Authenticated-Roles", "ADMIN,USER");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("gateway-admin");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void shouldBypassNonApiPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
