package ma.sirmaroc.matchs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.sirmaroc.matchs.dto.StadeRequest;
import ma.sirmaroc.matchs.dto.StadeResponse;
import ma.sirmaroc.matchs.exception.DuplicateResourceException;
import ma.sirmaroc.matchs.exception.ResourceNotFoundException;
import ma.sirmaroc.matchs.model.Stade;
import ma.sirmaroc.matchs.repository.StadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StadeService {

    private final StadeRepository stadeRepository;

    // ──────────── READ ────────────

    @Transactional(readOnly = true)
    public List<StadeResponse> findAll() {
        log.debug("Récupération de tous les stades");
        return stadeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StadeResponse findById(Integer id) {
        log.debug("Recherche du stade avec id={}", id);
        Stade stade = stadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stade", "id", id));
        return toResponse(stade);
    }

    @Transactional(readOnly = true)
    public List<StadeResponse> findByVille(String ville) {
        log.debug("Recherche des stades dans la ville={}", ville);
        return stadeRepository.findByVilleIgnoreCase(ville)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ──────────── CREATE ────────────

    public StadeResponse create(StadeRequest request) {
        log.info("Création d'un nouveau stade : {}", request.getNom());

        if (stadeRepository.existsByNomIgnoreCase(request.getNom())) {
            throw new DuplicateResourceException("Stade", "nom", request.getNom());
        }

        Stade stade = Stade.builder()
                .nom(request.getNom())
                .ville(request.getVille())
                .capacite(request.getCapacite())
                .build();

        Stade saved = stadeRepository.save(stade);
        log.info("Stade créé avec id={}", saved.getId());
        return toResponse(saved);
    }

    // ──────────── UPDATE ────────────

    public StadeResponse update(Integer id, StadeRequest request) {
        log.info("Mise à jour du stade id={}", id);

        Stade stade = stadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stade", "id", id));

        // Check duplicate name only if name changed
        if (!stade.getNom().equalsIgnoreCase(request.getNom())
                && stadeRepository.existsByNomIgnoreCase(request.getNom())) {
            throw new DuplicateResourceException("Stade", "nom", request.getNom());
        }

        stade.setNom(request.getNom());
        stade.setVille(request.getVille());
        stade.setCapacite(request.getCapacite());

        Stade updated = stadeRepository.save(stade);
        log.info("Stade id={} mis à jour avec succès", id);
        return toResponse(updated);
    }

    // ──────────── DELETE ────────────

    public void delete(Integer id) {
        log.info("Suppression du stade id={}", id);

        if (!stadeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stade", "id", id);
        }

        stadeRepository.deleteById(id);
        log.info("Stade id={} supprimé", id);
    }

    // ──────────── MAPPER ────────────

    public StadeResponse toResponse(Stade stade) {
        return StadeResponse.builder()
                .id(stade.getId())
                .nom(stade.getNom())
                .ville(stade.getVille())
                .capacite(stade.getCapacite())
                .build();
    }
}
