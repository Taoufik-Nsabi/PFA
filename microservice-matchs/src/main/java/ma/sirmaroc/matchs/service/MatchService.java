package ma.sirmaroc.matchs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.sirmaroc.matchs.dto.MatchRequest;
import ma.sirmaroc.matchs.dto.MatchResponse;
import ma.sirmaroc.matchs.dto.StatusMatchRequest;
import ma.sirmaroc.matchs.dto.StatusMatchResponse;
import ma.sirmaroc.matchs.exception.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchService {

    private final MatchRepository matchRepository;
    private final StadeRepository stadeRepository;
    private final EquipeRepository equipeRepository;
    private final StadeService stadeService;
    private final EquipeService equipeService;

    @Transactional(readOnly = true)
    public List<MatchResponse> findAll() {
        log.debug("Recuperation de tous les matchs");
        return matchRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatchResponse findById(Integer id) {
        log.debug("Recherche du match avec id={}", id);
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", id));
        return toResponse(match);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findByVille(String ville) {
        log.debug("Recherche des matchs dans la ville={}", ville);
        return matchRepository.findByVille(ville)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findByStade(Integer stadeId) {
        log.debug("Recherche des matchs du stade id={}", stadeId);
        return matchRepository.findByStadeId(stadeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findByEquipe(Integer equipeId) {
        log.debug("Recherche des matchs de l'equipe id={}", equipeId);
        return matchRepository.findByEquipeId(equipeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findByDate(LocalDate date) {
        log.debug("Recherche des matchs a la date={}", date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return matchRepository.findByDate(startOfDay, endOfDay)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findEnCours() {
        return findByEtat(MatchState.EN_COURS);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findUpcoming() {
        return findByEtat(MatchState.PROGRAMME);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findTermines() {
        return findByEtat(MatchState.TERMINE);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findReportes() {
        return findByEtat(MatchState.REPORTE);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findSuspendus() {
        return findByEtat(MatchState.SUSPENDU);
    }

    public MatchResponse create(MatchRequest request) {
        log.info("Creation d'un nouveau match");

        Stade stade = stadeRepository.findById(request.getStadeId())
                .orElseThrow(() -> new ResourceNotFoundException("Stade", "id", request.getStadeId()));

        Equipe equipeLocale = equipeRepository.findById(request.getEquipeLocaleId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe", "id", request.getEquipeLocaleId()));

        Equipe equipeVisiteur = equipeRepository.findById(request.getEquipeVisiteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe", "id", request.getEquipeVisiteurId()));

        validateTeams(equipeLocale, equipeVisiteur);

        StatusMatch statusMatch = buildStatusMatch(request.getStatusMatch());

        Match match = Match.builder()
                .dateHeure(request.getDateHeure())
                .stade(stade)
                .equipeLocale(equipeLocale)
                .equipeVisiteur(equipeVisiteur)
                .statusMatch(statusMatch)
                .build();

        Match saved = matchRepository.save(match);
        log.info("Match cree avec id={}", saved.getId());
        return toResponse(saved);
    }

    public MatchResponse update(Integer id, MatchRequest request) {
        log.info("Mise a jour du match id={}", id);

        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", id));

        Stade stade = stadeRepository.findById(request.getStadeId())
                .orElseThrow(() -> new ResourceNotFoundException("Stade", "id", request.getStadeId()));

        Equipe equipeLocale = equipeRepository.findById(request.getEquipeLocaleId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe", "id", request.getEquipeLocaleId()));

        Equipe equipeVisiteur = equipeRepository.findById(request.getEquipeVisiteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe", "id", request.getEquipeVisiteurId()));

        validateTeams(equipeLocale, equipeVisiteur);

        match.setDateHeure(request.getDateHeure());
        match.setStade(stade);
        match.setEquipeLocale(equipeLocale);
        match.setEquipeVisiteur(equipeVisiteur);

        if (request.getStatusMatch() != null) {
            StatusMatch status = match.getStatusMatch() != null
                    ? match.getStatusMatch()
                    : new StatusMatch();
            updateStatusMatch(status, request.getStatusMatch());
            match.setStatusMatch(status);
        }

        Match updated = matchRepository.save(match);
        log.info("Match id={} mis a jour avec succes", id);
        return toResponse(updated);
    }

    public MatchResponse updateStatus(Integer id, StatusMatchRequest statusRequest) {
        log.info("Mise a jour du statut du match id={}", id);

        if (statusRequest == null) {
            throw new IllegalArgumentException("Le statut du match est obligatoire");
        }

        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", id));

        StatusMatch status = match.getStatusMatch() != null
                ? match.getStatusMatch()
                : new StatusMatch();

        updateStatusMatch(status, statusRequest);
        match.setStatusMatch(status);

        Match updated = matchRepository.save(match);
        log.info("Statut du match id={} mis a jour", id);
        return toResponse(updated);
    }

    public void delete(Integer id) {
        log.info("Suppression du match id={}", id);

        if (!matchRepository.existsById(id)) {
            throw new ResourceNotFoundException("Match", "id", id);
        }

        matchRepository.deleteById(id);
        log.info("Match id={} supprime", id);
    }

    private void validateTeams(Equipe equipeLocale, Equipe equipeVisiteur) {
        if (equipeLocale.getId().equals(equipeVisiteur.getId())) {
            throw new IllegalArgumentException("L'equipe locale et l'equipe visiteur ne peuvent pas etre identiques");
        }
    }

    private StatusMatch buildStatusMatch(StatusMatchRequest request) {
        if (request == null) {
            return StatusMatch.builder()
                    .dureeMinutes(0)
                    .score("0-0")
                    .etat(MatchState.PROGRAMME)
                    .build();
        }

        return StatusMatch.builder()
                .dureeMinutes(request.getDureeMinutes() != null ? request.getDureeMinutes() : 0)
                .score(request.getScore() != null ? request.getScore() : "0-0")
                .etat(request.getEtat())
                .build();
    }

    private void updateStatusMatch(StatusMatch status, StatusMatchRequest request) {
        if (request.getDureeMinutes() != null) {
            status.setDureeMinutes(request.getDureeMinutes());
        }
        if (request.getScore() != null) {
            status.setScore(request.getScore());
        }
        status.setEtat(request.getEtat());
    }

    private List<MatchResponse> findByEtat(MatchState etat) {
        return matchRepository.findByStatusMatchEtat(etat)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MatchResponse toResponse(Match match) {
        StatusMatchResponse statusResp = null;
        if (match.getStatusMatch() != null) {
            StatusMatch status = match.getStatusMatch();
            statusResp = StatusMatchResponse.builder()
                    .id(status.getId())
                    .dureeMinutes(status.getDureeMinutes())
                    .score(status.getScore())
                    .etat(status.getEtat())
                    .build();
        }

        return MatchResponse.builder()
                .id(match.getId())
                .dateHeure(match.getDateHeure())
                .stade(stadeService.toResponse(match.getStade()))
                .equipeLocale(equipeService.toResponse(match.getEquipeLocale()))
                .equipeVisiteur(equipeService.toResponse(match.getEquipeVisiteur()))
                .statusMatch(statusResp)
                .build();
    }
}
