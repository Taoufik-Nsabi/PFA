package ma.sirmaroc.matchs.integration.apifootball;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.api-football")
public class ApiFootballProperties {

    private boolean enabled = false;
    private String baseUrl = "https://v3.football.api-sports.io";
    private String apiKey;
    private String timezone = "Africa/Casablanca";
}
