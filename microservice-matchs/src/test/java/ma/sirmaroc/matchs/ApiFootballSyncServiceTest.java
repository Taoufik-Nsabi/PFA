package ma.sirmaroc.matchs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.sirmaroc.matchs.dto.ApiFootballFixturesSyncRequest;
import ma.sirmaroc.matchs.dto.ApiFootballSyncSummary;
import ma.sirmaroc.matchs.dto.ApiFootballTeamsSyncRequest;
import ma.sirmaroc.matchs.integration.apifootball.ApiFootballClient;
import ma.sirmaroc.matchs.model.Equipe;
import ma.sirmaroc.matchs.model.Match;
import ma.sirmaroc.matchs.model.MatchState;
import ma.sirmaroc.matchs.model.Stade;
import ma.sirmaroc.matchs.repository.EquipeRepository;
import ma.sirmaroc.matchs.repository.MatchRepository;
import ma.sirmaroc.matchs.repository.StadeRepository;
import ma.sirmaroc.matchs.service.ApiFootballSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - ApiFootballSyncService")
class ApiFootballSyncServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ApiFootballClient apiFootballClient;

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private StadeRepository stadeRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private ApiFootballSyncService apiFootballSyncService;

    @Test
    @DisplayName("syncTeams() doit creer une equipe et un stade a partir de l'API")
    void syncTeams_ShouldCreateTeamAndStade() throws Exception {
        JsonNode apiResponse = objectMapper.readTree("""
                {
                  "response": [
                    {
                      "team": {
                        "id": 101,
                        "name": "Maroc",
                        "country": "Maroc"
                      },
                      "venue": {
                        "id": 501,
                        "name": "Grand Stade de Casablanca",
                        "city": "Casablanca",
                        "capacity": 93000
                      }
                    }
                  ],
                  "errors": []
                }
                """);

        when(apiFootballClient.getTeamsByLeagueAndSeason(39, 2026)).thenReturn(apiResponse);
        when(stadeRepository.findByApiFootballId(501)).thenReturn(Optional.empty());
        when(stadeRepository.findByNomIgnoreCase("Grand Stade de Casablanca")).thenReturn(Optional.empty());
        when(equipeRepository.findByApiFootballId(101)).thenReturn(Optional.empty());
        when(equipeRepository.findByNameIgnoreCase("Maroc")).thenReturn(Optional.empty());
        when(stadeRepository.save(any(Stade.class))).thenAnswer(invocation -> {
            Stade stade = invocation.getArgument(0);
            stade.setId(10);
            return stade;
        });
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> {
            Equipe equipe = invocation.getArgument(0);
            equipe.setId(20);
            return equipe;
        });

        ApiFootballSyncSummary summary = apiFootballSyncService.syncTeams(ApiFootballTeamsSyncRequest.builder()
                .leagueId(39)
                .season(2026)
                .build());

        assertThat(summary.getLeagueId()).isEqualTo(39);
        assertThat(summary.getSeason()).isEqualTo(2026);
        assertThat(summary.getTeamsProcessed()).isEqualTo(1);
        assertThat(summary.getTeamsCreated()).isEqualTo(1);
        assertThat(summary.getTeamsUpdated()).isZero();
        assertThat(summary.getStadesProcessed()).isEqualTo(1);
        assertThat(summary.getStadesCreated()).isEqualTo(1);
        assertThat(summary.getStadesUpdated()).isZero();

        ArgumentCaptor<Equipe> equipeCaptor = ArgumentCaptor.forClass(Equipe.class);
        ArgumentCaptor<Stade> stadeCaptor = ArgumentCaptor.forClass(Stade.class);

        verify(equipeRepository).save(equipeCaptor.capture());
        verify(stadeRepository).save(stadeCaptor.capture());

        assertThat(equipeCaptor.getValue().getApiFootballId()).isEqualTo(101);
        assertThat(equipeCaptor.getValue().getName()).isEqualTo("Maroc");
        assertThat(equipeCaptor.getValue().getPays()).isEqualTo("Maroc");

        assertThat(stadeCaptor.getValue().getApiFootballId()).isEqualTo(501);
        assertThat(stadeCaptor.getValue().getNom()).isEqualTo("Grand Stade de Casablanca");
        assertThat(stadeCaptor.getValue().getVille()).isEqualTo("Casablanca");
        assertThat(stadeCaptor.getValue().getCapacite()).isEqualTo(93000);
    }

    @Test
    @DisplayName("syncFixtures() doit creer un match complet depuis l'API")
    void syncFixtures_ShouldCreateMatchFromApiResponse() throws Exception {
        JsonNode apiResponse = objectMapper.readTree("""
                {
                  "response": [
                    {
                      "fixture": {
                        "id": 9001,
                        "date": "2030-06-15T18:00:00+01:00",
                        "venue": {
                          "id": 501,
                          "name": "Grand Stade de Casablanca",
                          "city": "Casablanca",
                          "capacity": 93000
                        },
                        "status": {
                          "short": "FT",
                          "elapsed": 90
                        }
                      },
                      "teams": {
                        "home": {
                          "id": 101,
                          "name": "Maroc"
                        },
                        "away": {
                          "id": 102,
                          "name": "France"
                        }
                      },
                      "goals": {
                        "home": 2,
                        "away": 1
                      }
                    }
                  ],
                  "errors": []
                }
                """);

        AtomicInteger stadeIds = new AtomicInteger(10);
        AtomicInteger equipeIds = new AtomicInteger(20);

        when(apiFootballClient.getFixtures(
                eq(39),
                eq(2030),
                eq(LocalDate.of(2030, 6, 15)),
                eq(LocalDate.of(2030, 6, 15))))
                .thenReturn(apiResponse);

        when(stadeRepository.findByApiFootballId(501)).thenReturn(Optional.empty());
        when(stadeRepository.findByNomIgnoreCase("Grand Stade de Casablanca")).thenReturn(Optional.empty());
        when(stadeRepository.save(any(Stade.class))).thenAnswer(invocation -> {
            Stade stade = invocation.getArgument(0);
            stade.setId(stadeIds.getAndIncrement());
            return stade;
        });

        when(equipeRepository.findByApiFootballId(101)).thenReturn(Optional.empty());
        when(equipeRepository.findByApiFootballId(102)).thenReturn(Optional.empty());
        when(equipeRepository.findByNameIgnoreCase("Maroc")).thenReturn(Optional.empty());
        when(equipeRepository.findByNameIgnoreCase("France")).thenReturn(Optional.empty());
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> {
            Equipe equipe = invocation.getArgument(0);
            equipe.setId(equipeIds.getAndIncrement());
            return equipe;
        });

        when(matchRepository.findByApiFootballId(9001)).thenReturn(Optional.empty());
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiFootballSyncSummary summary = apiFootballSyncService.syncFixtures(ApiFootballFixturesSyncRequest.builder()
                .leagueId(39)
                .season(2030)
                .fromDate(LocalDate.of(2030, 6, 15))
                .toDate(LocalDate.of(2030, 6, 15))
                .syncTeamsBeforeFixtures(false)
                .build());

        assertThat(summary.isSyncedTeamsBeforeFixtures()).isFalse();
        assertThat(summary.getMatchesProcessed()).isEqualTo(1);
        assertThat(summary.getMatchesCreated()).isEqualTo(1);
        assertThat(summary.getMatchesUpdated()).isZero();

        ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(matchCaptor.capture());

        Match savedMatch = matchCaptor.getValue();
        assertThat(savedMatch.getApiFootballId()).isEqualTo(9001);
        assertThat(savedMatch.getDateHeure()).isEqualTo(LocalDateTime.of(2030, 6, 15, 18, 0));
        assertThat(savedMatch.getStade()).isNotNull();
        assertThat(savedMatch.getStade().getApiFootballId()).isEqualTo(501);
        assertThat(savedMatch.getEquipeLocale()).isNotNull();
        assertThat(savedMatch.getEquipeLocale().getApiFootballId()).isEqualTo(101);
        assertThat(savedMatch.getEquipeVisiteur()).isNotNull();
        assertThat(savedMatch.getEquipeVisiteur().getApiFootballId()).isEqualTo(102);
        assertThat(savedMatch.getStatusMatch()).isNotNull();
        assertThat(savedMatch.getStatusMatch().getEtat()).isEqualTo(MatchState.TERMINE);
        assertThat(savedMatch.getStatusMatch().getDureeMinutes()).isEqualTo(90);
        assertThat(savedMatch.getStatusMatch().getScore()).isEqualTo("2-1");
    }

    @Test
    @DisplayName("syncFixtures() doit ignorer un match incomplet")
    void syncFixtures_ShouldSkipFixture_WhenMandatoryDataIsMissing() throws Exception {
        JsonNode apiResponse = objectMapper.readTree("""
                {
                  "response": [
                    {
                      "fixture": {
                        "id": 9002,
                        "date": "2030-06-16T21:00:00+01:00",
                        "venue": {},
                        "status": {
                          "short": "NS",
                          "elapsed": null
                        }
                      },
                      "teams": {
                        "home": {
                          "id": 201,
                          "name": "Espagne"
                        },
                        "away": {
                          "id": 202,
                          "name": "Portugal"
                        }
                      },
                      "goals": {
                        "home": null,
                        "away": null
                      }
                    }
                  ],
                  "errors": []
                }
                """);

        AtomicInteger equipeIds = new AtomicInteger(50);

        when(apiFootballClient.getFixtures(
                eq(39),
                eq(2030),
                eq(LocalDate.of(2030, 6, 16)),
                eq(LocalDate.of(2030, 6, 16))))
                .thenReturn(apiResponse);
        when(equipeRepository.findByApiFootballId(201)).thenReturn(Optional.empty());
        when(equipeRepository.findByApiFootballId(202)).thenReturn(Optional.empty());
        when(equipeRepository.findByNameIgnoreCase("Espagne")).thenReturn(Optional.empty());
        when(equipeRepository.findByNameIgnoreCase("Portugal")).thenReturn(Optional.empty());
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> {
            Equipe equipe = invocation.getArgument(0);
            equipe.setId(equipeIds.getAndIncrement());
            return equipe;
        });

        ApiFootballSyncSummary summary = apiFootballSyncService.syncFixtures(ApiFootballFixturesSyncRequest.builder()
                .leagueId(39)
                .season(2030)
                .fromDate(LocalDate.of(2030, 6, 16))
                .toDate(LocalDate.of(2030, 6, 16))
                .syncTeamsBeforeFixtures(false)
                .build());

        assertThat(summary.getMatchesProcessed()).isZero();
        assertThat(summary.getMatchesCreated()).isZero();
        assertThat(summary.getMatchesUpdated()).isZero();

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("syncFixtures() doit mapper un match suspendu vers l'etat SUSPENDU")
    void syncFixtures_ShouldMapSuspendedFixtureStatus() throws Exception {
        JsonNode apiResponse = objectMapper.readTree("""
                {
                  "response": [
                    {
                      "fixture": {
                        "id": 9003,
                        "date": "2030-06-17T19:00:00+01:00",
                        "venue": {
                          "id": 601,
                          "name": "Stade de Rabat",
                          "city": "Rabat",
                          "capacity": 52000
                        },
                        "status": {
                          "short": "SUSP",
                          "elapsed": 67
                        }
                      },
                      "teams": {
                        "home": {
                          "id": 301,
                          "name": "Maroc"
                        },
                        "away": {
                          "id": 302,
                          "name": "Portugal"
                        }
                      },
                      "goals": {
                        "home": 1,
                        "away": 1
                      }
                    }
                  ],
                  "errors": []
                }
                """);

        AtomicInteger stadeIds = new AtomicInteger(60);
        AtomicInteger equipeIds = new AtomicInteger(70);

        when(apiFootballClient.getFixtures(
                eq(39),
                eq(2030),
                eq(LocalDate.of(2030, 6, 17)),
                eq(LocalDate.of(2030, 6, 17))))
                .thenReturn(apiResponse);

        when(stadeRepository.findByApiFootballId(601)).thenReturn(Optional.empty());
        when(stadeRepository.findByNomIgnoreCase("Stade de Rabat")).thenReturn(Optional.empty());
        when(stadeRepository.save(any(Stade.class))).thenAnswer(invocation -> {
            Stade stade = invocation.getArgument(0);
            stade.setId(stadeIds.getAndIncrement());
            return stade;
        });

        when(equipeRepository.findByApiFootballId(301)).thenReturn(Optional.empty());
        when(equipeRepository.findByApiFootballId(302)).thenReturn(Optional.empty());
        when(equipeRepository.findByNameIgnoreCase("Maroc")).thenReturn(Optional.empty());
        when(equipeRepository.findByNameIgnoreCase("Portugal")).thenReturn(Optional.empty());
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> {
            Equipe equipe = invocation.getArgument(0);
            equipe.setId(equipeIds.getAndIncrement());
            return equipe;
        });

        when(matchRepository.findByApiFootballId(9003)).thenReturn(Optional.empty());
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        apiFootballSyncService.syncFixtures(ApiFootballFixturesSyncRequest.builder()
                .leagueId(39)
                .season(2030)
                .fromDate(LocalDate.of(2030, 6, 17))
                .toDate(LocalDate.of(2030, 6, 17))
                .syncTeamsBeforeFixtures(false)
                .build());

        ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(matchCaptor.capture());

        assertThat(matchCaptor.getValue().getStatusMatch().getEtat()).isEqualTo(MatchState.SUSPENDU);
    }
}
