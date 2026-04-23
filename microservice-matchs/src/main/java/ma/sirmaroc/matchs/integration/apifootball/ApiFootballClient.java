package ma.sirmaroc.matchs.integration.apifootball;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ApiFootballClient {

    private final RestClient apiFootballRestClient;
    private final ApiFootballProperties properties;

    public JsonNode getFixtures(Integer leagueId, Integer season, LocalDate fromDate, LocalDate toDate) {
        ensureConfigured();
        return apiFootballRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures")
                        .queryParam("league", leagueId)
                        .queryParam("season", season)
                        .queryParam("from", fromDate)
                        .queryParam("to", toDate)
                        .queryParam("timezone", properties.getTimezone())
                        .build())
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getFixturesByDate(LocalDate date) {
        ensureConfigured();
        return apiFootballRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures")
                        .queryParam("date", date)
                        .queryParam("timezone", properties.getTimezone())
                        .build())
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getTeamsByLeagueAndSeason(Integer leagueId, Integer season) {
        ensureConfigured();
        return apiFootballRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("league", leagueId)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .body(JsonNode.class);
    }

    private void ensureConfigured() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("API-Football integration is disabled. Set API_FOOTBALL_ENABLED=true to use it.");
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("API_FOOTBALL_API_KEY must be configured when API-Football integration is enabled.");
        }
    }
}
