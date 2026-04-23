package ma.sirmaroc.matchs.integration.apifootball;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiFootballConfig {

    @Bean
    public RestClient apiFootballRestClient(RestClient.Builder builder, ApiFootballProperties properties) {
        RestClient.Builder configuredBuilder = builder.baseUrl(properties.getBaseUrl());

        if (StringUtils.hasText(properties.getApiKey())) {
            configuredBuilder = configuredBuilder.defaultHeader("x-apisports-key", properties.getApiKey());
        }

        return configuredBuilder.build();
    }
}
