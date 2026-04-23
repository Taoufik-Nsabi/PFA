package ma.sirmaroc.matchs.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.sirmaroc.matchs.dto.ApiFootballFixturesSyncRequest;
import ma.sirmaroc.matchs.dto.ApiFootballSyncSummary;
import ma.sirmaroc.matchs.dto.ApiFootballTeamsSyncRequest;
import ma.sirmaroc.matchs.integration.apifootball.ApiFootballClient;
import ma.sirmaroc.matchs.model.Equipe;
import ma.sirmaroc.matchs.model.Match;
import ma.sirmaroc.matchs.model.MatchState;
import ma.sirmaroc.matchs.model.Stade;
import ma.sirmaroc.matchs.model.StatusMatch;
import ma.sirmaroc.matchs.repository.EquipeRepository;
import ma.sirmaroc.matchs.repository.MatchRepository;
import ma.sirmaroc.matchs.repository.StadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApiFootballSyncService {

    private static final String UNKNOWN_COUNTRY = "Unknown";

    private final ApiFootballClient apiFootballClient;
    private final EquipeRepository equipeRepository;
    private final StadeRepository stadeRepository;
    private final MatchRepository matchRepository;

    public ApiFootballSyncSummary syncTeams(ApiFootballTeamsSyncRequest request) {
        log.info("Sync API-Football teams for league={} season={}", request.getLeagueId(), request.getSeason());

        ApiFootballSyncSummary summary = ApiFootballSyncSummary.builder()
                .leagueId(request.getLeagueId())
                .season(request.getSeason())
                .build();

        JsonNode root = apiFootballClient.getTeamsByLeagueAndSeason(request.getLeagueId(), request.getSeason());
        validateApiResponse(root);

        for (JsonNode item : root.path("response")) {
            PersistResult<Stade> stadeResult = upsertStade(item.path("venue"));
            if (stadeResult.processed()) {
                summary.incrementStadesProcessed();
                if (stadeResult.created()) {
                    summary.incrementStadesCreated();
                } else if (stadeResult.updated()) {
                    summary.incrementStadesUpdated();
                }
            }

            PersistResult<Equipe> teamResult = upsertTeam(item.path("team"));
            if (teamResult.processed()) {
                summary.incrementTeamsProcessed();
                if (teamResult.created()) {
                    summary.incrementTeamsCreated();
                } else if (teamResult.updated()) {
                    summary.incrementTeamsUpdated();
                }
            }
        }

        return summary;
    }

    public ApiFootballSyncSummary syncFixtures(ApiFootballFixturesSyncRequest request) {
        log.info(
                "Sync API-Football fixtures for league={} season={} from={} to={}",
                request.getLeagueId(),
                request.getSeason(),
                request.getFromDate(),
                request.getToDate()
        );

        ApiFootballSyncSummary summary = ApiFootballSyncSummary.builder()
                .leagueId(request.getLeagueId())
                .season(request.getSeason())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .syncedTeamsBeforeFixtures(Boolean.TRUE.equals(request.getSyncTeamsBeforeFixtures()))
                .build();

        if (Boolean.TRUE.equals(request.getSyncTeamsBeforeFixtures())) {
            summary.merge(syncTeams(ApiFootballTeamsSyncRequest.builder()
                    .leagueId(request.getLeagueId())
                    .season(request.getSeason())
                    .build()));
        }

        JsonNode root = apiFootballClient.getFixtures(
                request.getLeagueId(),
                request.getSeason(),
                request.getFromDate(),
                request.getToDate()
        );
        validateApiResponse(root);

        for (JsonNode item : root.path("response")) {
            PersistResult<Match> matchResult = upsertMatch(item);
            if (matchResult.processed()) {
                summary.incrementMatchesProcessed();
                if (matchResult.created()) {
                    summary.incrementMatchesCreated();
                } else if (matchResult.updated()) {
                    summary.incrementMatchesUpdated();
                }
            }
        }

        return summary;
    }

    private PersistResult<Match> upsertMatch(JsonNode fixtureResponse) {
        JsonNode fixtureNode = fixtureResponse.path("fixture");
        Integer fixtureId = readNullableInt(fixtureNode.path("id"));
        if (fixtureId == null) {
            return PersistResult.notProcessed();
        }

        PersistResult<Stade> stadeResult = upsertStade(fixtureNode.path("venue"));
        PersistResult<Equipe> homeTeamResult = resolveTeamForFixture(fixtureResponse.path("teams").path("home"));
        PersistResult<Equipe> awayTeamResult = resolveTeamForFixture(fixtureResponse.path("teams").path("away"));
        LocalDateTime fixtureDate = parseFixtureDate(fixtureNode.path("date").asText(null));

        if (fixtureDate == null || stadeResult.entity() == null || homeTeamResult.entity() == null || awayTeamResult.entity() == null) {
            log.warn("Skipping fixture {} because required data is missing", fixtureId);
            return PersistResult.notProcessed();
        }

        if (Objects.equals(homeTeamResult.entity().getId(), awayTeamResult.entity().getId())) {
            log.warn("Skipping fixture {} because home and away teams resolve to the same team", fixtureId);
            return PersistResult.notProcessed();
        }

        Match match = matchRepository.findByApiFootballId(fixtureId).orElseGet(Match::new);
        boolean created = match.getId() == null;
        boolean updated = false;

        updated |= assign(match.getApiFootballId(), fixtureId, match::setApiFootballId);
        updated |= assign(match.getDateHeure(), fixtureDate, match::setDateHeure);
        updated |= assignById(match.getStade(), stadeResult.entity(), Stade::getId, match::setStade);
        updated |= assignById(match.getEquipeLocale(), homeTeamResult.entity(), Equipe::getId, match::setEquipeLocale);
        updated |= assignById(match.getEquipeVisiteur(), awayTeamResult.entity(), Equipe::getId, match::setEquipeVisiteur);

        StatusMatch status = match.getStatusMatch() != null ? match.getStatusMatch() : new StatusMatch();
        boolean statusUpdated = false;
        statusUpdated |= assign(status.getEtat(), mapMatchState(fixtureNode.path("status").path("short").asText(null)), status::setEtat);
        statusUpdated |= assign(status.getDureeMinutes(), readNullableInt(fixtureNode.path("status").path("elapsed")), status::setDureeMinutes);
        statusUpdated |= assign(status.getScore(), buildScore(fixtureResponse.path("goals")), status::setScore);

        match.setStatusMatch(status);

        if (created || updated || statusUpdated) {
            Match saved = matchRepository.save(match);
            return PersistResult.processed(saved, created, updated || statusUpdated);
        }

        return PersistResult.processed(match, false, false);
    }

    private PersistResult<Equipe> resolveTeamForFixture(JsonNode teamNode) {
        Integer teamId = readNullableInt(teamNode.path("id"));
        if (teamId != null) {
            PersistResult<Equipe> existing = upsertTeam(teamNode);
            if (existing.processed()) {
                return existing;
            }
        }

        String teamName = textOrNull(teamNode.path("name"));
        if (!StringUtils.hasText(teamName)) {
            return PersistResult.notProcessed();
        }

        Equipe equipe = equipeRepository.findByNameIgnoreCase(teamName).orElseGet(Equipe::new);
        boolean created = equipe.getId() == null;
        boolean updated = false;

        updated |= assign(equipe.getApiFootballId(), teamId, equipe::setApiFootballId);
        updated |= assign(equipe.getName(), teamName, equipe::setName);
        updated |= assign(equipe.getPays(), UNKNOWN_COUNTRY, equipe::setPays);

        if (created || updated) {
            Equipe saved = equipeRepository.save(equipe);
            return PersistResult.processed(saved, created, !created);
        }

        return PersistResult.processed(equipe, false, false);
    }

    private PersistResult<Equipe> upsertTeam(JsonNode teamNode) {
        Integer teamId = readNullableInt(teamNode.path("id"));
        String teamName = textOrNull(teamNode.path("name"));

        Equipe equipe = null;
        if (teamId != null) {
            equipe = equipeRepository.findByApiFootballId(teamId).orElse(null);
        }
        if (equipe == null && StringUtils.hasText(teamName)) {
            equipe = equipeRepository.findByNameIgnoreCase(teamName).orElse(null);
        }
        if (equipe == null) {
            if (!StringUtils.hasText(teamName)) {
                return PersistResult.notProcessed();
            }
            equipe = new Equipe();
        }

        boolean created = equipe.getId() == null;
        boolean updated = false;

        updated |= assign(equipe.getApiFootballId(), teamId, equipe::setApiFootballId);
        if (StringUtils.hasText(teamName)) {
            updated |= assign(equipe.getName(), teamName, equipe::setName);
        } else if (created) {
            return PersistResult.notProcessed();
        }
        updated |= assign(equipe.getPays(), defaultString(textOrNull(teamNode.path("country")), UNKNOWN_COUNTRY), equipe::setPays);

        if (created || updated) {
            Equipe saved = equipeRepository.save(equipe);
            return PersistResult.processed(saved, created, !created);
        }

        return PersistResult.processed(equipe, false, false);
    }

    private PersistResult<Stade> upsertStade(JsonNode venueNode) {
        Integer venueId = readNullableInt(venueNode.path("id"));
        String venueName = textOrNull(venueNode.path("name"));

        Stade stade = null;
        if (venueId != null) {
            stade = stadeRepository.findByApiFootballId(venueId).orElse(null);
        }
        if (stade == null && StringUtils.hasText(venueName)) {
            stade = stadeRepository.findByNomIgnoreCase(venueName).orElse(null);
        }
        if (stade == null) {
            if (!StringUtils.hasText(venueName)) {
                return PersistResult.notProcessed();
            }
            stade = new Stade();
        }

        boolean created = stade.getId() == null;
        boolean updated = false;

        updated |= assign(stade.getApiFootballId(), venueId, stade::setApiFootballId);
        if (StringUtils.hasText(venueName)) {
            updated |= assign(stade.getNom(), venueName, stade::setNom);
        } else if (created) {
            return PersistResult.notProcessed();
        }
        updated |= assign(stade.getVille(), defaultString(textOrNull(venueNode.path("city")), "Unknown"), stade::setVille);
        updated |= assign(stade.getCapacite(), defaultInteger(readNullableInt(venueNode.path("capacity")), 1), stade::setCapacite);

        if (created || updated) {
            Stade saved = stadeRepository.save(stade);
            return PersistResult.processed(saved, created, !created);
        }

        return PersistResult.processed(stade, false, false);
    }

    private MatchState mapMatchState(String statusShort) {
        if (!StringUtils.hasText(statusShort)) {
            return MatchState.PROGRAMME;
        }

        return switch (statusShort) {
            case "NS", "TBD" -> MatchState.PROGRAMME;
            case "1H", "HT", "2H", "ET", "BT", "P", "INT", "LIVE" -> MatchState.EN_COURS;
            case "FT", "AET", "PEN" -> MatchState.TERMINE;
            case "PST" -> MatchState.REPORTE;
            case "CANC", "ABD", "AWD", "WO" -> MatchState.ANNULE;
            case "SUSP" -> MatchState.SUSPENDU;
            default -> MatchState.PROGRAMME;
        };
    }

    private LocalDateTime parseFixtureDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            return LocalDateTime.parse(value);
        }
    }

    private String buildScore(JsonNode goalsNode) {
        Integer home = readNullableInt(goalsNode.path("home"));
        Integer away = readNullableInt(goalsNode.path("away"));

        if (home == null && away == null) {
            return "0-0";
        }

        return defaultInteger(home, 0) + "-" + defaultInteger(away, 0);
    }

    private void validateApiResponse(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            throw new IllegalStateException("API-Football returned an empty response.");
        }

        JsonNode errors = root.path("errors");
        if ((errors.isObject() && errors.fields().hasNext()) || (errors.isArray() && errors.size() > 0)) {
            throw new IllegalStateException("API-Football returned errors: " + errors);
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Integer readNullableInt(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isIntegralNumber()) {
            return node.asInt();
        }
        String value = node.asText(null);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Integer.valueOf(value);
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private Integer defaultInteger(Integer value, Integer fallback) {
        return value != null ? value : fallback;
    }

    private <T> boolean assign(T currentValue, T newValue, Consumer<T> consumer) {
        if (!Objects.equals(currentValue, newValue)) {
            consumer.accept(newValue);
            return true;
        }
        return false;
    }

    private <T> boolean assignById(T currentValue, T newValue, Function<T, Integer> idExtractor, Consumer<T> consumer) {
        Integer currentId = currentValue != null ? idExtractor.apply(currentValue) : null;
        Integer newId = newValue != null ? idExtractor.apply(newValue) : null;
        if (!Objects.equals(currentId, newId)) {
            consumer.accept(newValue);
            return true;
        }
        return false;
    }

    private record PersistResult<T>(T entity, boolean processed, boolean created, boolean updated) {
        static <T> PersistResult<T> notProcessed() {
            return new PersistResult<>(null, false, false, false);
        }

        static <T> PersistResult<T> processed(T entity, boolean created, boolean updated) {
            return new PersistResult<>(entity, true, created, updated);
        }
    }
}
