package ma.sirmaroc.matchs;

import ma.sirmaroc.matchs.dto.MatchRequest;
import ma.sirmaroc.matchs.dto.MatchResponse;
import ma.sirmaroc.matchs.dto.StatusMatchRequest;
import ma.sirmaroc.matchs.exception.ResourceNotFoundException;
import ma.sirmaroc.matchs.model.Equipe;
import ma.sirmaroc.matchs.model.Match;
import ma.sirmaroc.matchs.model.MatchState;
import ma.sirmaroc.matchs.model.Stade;
import ma.sirmaroc.matchs.model.StatusMatch;
import ma.sirmaroc.matchs.repository.EquipeRepository;
import ma.sirmaroc.matchs.repository.MatchRepository;
import ma.sirmaroc.matchs.repository.StadeRepository;
import ma.sirmaroc.matchs.service.EquipeService;
import ma.sirmaroc.matchs.service.MatchService;
import ma.sirmaroc.matchs.service.StadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - MatchService")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private StadeRepository stadeRepository;

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private StadeService stadeService;

    @Mock
    private EquipeService equipeService;

    @InjectMocks
    private MatchService matchService;

    private Stade stade;
    private Equipe equipeLocale;
    private Equipe equipeVisiteur;
    private StatusMatch statusMatch;
    private Match match;

    @BeforeEach
    void setUp() {
        stade = Stade.builder()
                .id(1).nom("Grand Stade Casablanca").ville("Casablanca").capacite(93000)
                .build();

        equipeLocale = Equipe.builder()
                .id(1).name("Maroc").pays("Maroc").entraineur("Regragui")
                .domicile(true).exterieur(false)
                .build();

        equipeVisiteur = Equipe.builder()
                .id(2).name("France").pays("France").entraineur("Deschamps")
                .domicile(false).exterieur(true)
                .build();

        statusMatch = StatusMatch.builder()
                .id(1)
                .dureeMinutes(0)
                .score("0-0")
                .etat(MatchState.PROGRAMME)
                .build();

        match = Match.builder()
                .id(1)
                .dateHeure(LocalDateTime.of(2030, 6, 15, 18, 0))
                .stade(stade)
                .equipeLocale(equipeLocale)
                .equipeVisiteur(equipeVisiteur)
                .statusMatch(statusMatch)
                .build();
    }

    @Test
    @DisplayName("findAll() doit retourner tous les matchs")
    void findAll_ShouldReturnAllMatches() {
        when(matchRepository.findAll()).thenReturn(List.of(match));
        when(stadeService.toResponse(any())).thenCallRealMethod();
        when(equipeService.toResponse(any())).thenCallRealMethod();

        List<MatchResponse> result = matchService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        verify(matchRepository).findAll();
    }

    @Test
    @DisplayName("findById() doit retourner le match si trouve")
    void findById_ShouldReturnMatch_WhenExists() {
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(stadeService.toResponse(any())).thenCallRealMethod();
        when(equipeService.toResponse(any())).thenCallRealMethod();

        MatchResponse result = matchService.findById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getDateHeure()).isEqualTo(LocalDateTime.of(2030, 6, 15, 18, 0));
    }

    @Test
    @DisplayName("findByDate() doit retourner les matchs de la journee")
    void findByDate_ShouldReturnMatchesForGivenDay() {
        when(matchRepository.findByDate(
                LocalDateTime.of(2030, 6, 15, 0, 0),
                LocalDateTime.of(2030, 6, 16, 0, 0)))
                .thenReturn(List.of(match));
        when(stadeService.toResponse(any())).thenCallRealMethod();
        when(equipeService.toResponse(any())).thenCallRealMethod();

        List<MatchResponse> result = matchService.findByDate(LocalDate.of(2030, 6, 15));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDateHeure()).isEqualTo(LocalDateTime.of(2030, 6, 15, 18, 0));
    }

    @Test
    @DisplayName("findById() doit lever ResourceNotFoundException si non trouve")
    void findById_ShouldThrow_WhenNotFound() {
        when(matchRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.findById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Match");
    }

    @Test
    @DisplayName("create() doit rejeter si les deux equipes sont identiques")
    void create_ShouldThrow_WhenSameTeams() {
        MatchRequest request = MatchRequest.builder()
                .dateHeure(LocalDateTime.of(2030, 6, 15, 18, 0))
                .stadeId(1)
                .equipeLocaleId(1)
                .equipeVisiteurId(1)
                .build();

        when(stadeRepository.findById(1)).thenReturn(Optional.of(stade));
        when(equipeRepository.findById(1)).thenReturn(Optional.of(equipeLocale));

        assertThatThrownBy(() -> matchService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identiques");
    }

    @Test
    @DisplayName("create() doit initialiser un statut par defaut si absent")
    void create_ShouldCreateDefaultStatus_WhenStatusMissing() {
        MatchRequest request = MatchRequest.builder()
                .dateHeure(LocalDateTime.of(2030, 6, 15, 18, 0))
                .stadeId(1)
                .equipeLocaleId(1)
                .equipeVisiteurId(2)
                .build();

        when(stadeRepository.findById(1)).thenReturn(Optional.of(stade));
        when(equipeRepository.findById(1)).thenReturn(Optional.of(equipeLocale));
        when(equipeRepository.findById(2)).thenReturn(Optional.of(equipeVisiteur));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match savedMatch = invocation.getArgument(0);
            savedMatch.setId(10);
            return savedMatch;
        });
        when(stadeService.toResponse(any())).thenCallRealMethod();
        when(equipeService.toResponse(any())).thenCallRealMethod();

        MatchResponse result = matchService.create(request);

        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getStatusMatch()).isNotNull();
        assertThat(result.getStatusMatch().getScore()).isEqualTo("0-0");
        assertThat(result.getStatusMatch().getEtat()).isEqualTo(MatchState.PROGRAMME);
    }

    @Test
    @DisplayName("delete() doit lever ResourceNotFoundException si match inexistant")
    void delete_ShouldThrow_WhenNotFound() {
        when(matchRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> matchService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateStatus() doit mettre a jour le statut du match")
    void updateStatus_ShouldUpdateMatchStatus() {
        StatusMatchRequest statusRequest = StatusMatchRequest.builder()
                .etat(MatchState.EN_COURS)
                .score("1-0")
                .dureeMinutes(45)
                .build();

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(matchRepository.save(any())).thenReturn(match);
        when(stadeService.toResponse(any())).thenCallRealMethod();
        when(equipeService.toResponse(any())).thenCallRealMethod();

        MatchResponse result = matchService.updateStatus(1, statusRequest);

        assertThat(result).isNotNull();
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    @DisplayName("updateStatus() doit rejeter un payload vide")
    void updateStatus_ShouldThrow_WhenStatusIsMissing() {
        assertThatThrownBy(() -> matchService.updateStatus(1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoire");
    }
}
