package ma.sirmaroc.matchs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.sirmaroc.matchs.dto.EquipeRequest;
import ma.sirmaroc.matchs.dto.EquipeResponse;
import ma.sirmaroc.matchs.exception.DuplicateResourceException;
import ma.sirmaroc.matchs.exception.ResourceNotFoundException;
import ma.sirmaroc.matchs.model.Equipe;
import ma.sirmaroc.matchs.repository.EquipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipeService {

    private final EquipeRepository equipeRepository;

    // ──────────── READ ────────────

    @Transactional(readOnly = true)
    public List<EquipeResponse> findAll() {
        log.debug("Récupération de toutes les équipes");
        return equipeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipeResponse findById(Integer id) {
        log.debug("Recherche de l'équipe avec id={}", id);
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe", "id", id));
        return toResponse(equipe);
    }

    @Transactional(readOnly = true)
    public List<EquipeResponse> findByPays(String pays) {
        log.debug("Recherche des équipes du pays={}", pays);
        return equipeRepository.findByPaysIgnoreCase(pays)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ──────────── CREATE ────────────

    public EquipeResponse create(EquipeRequest request) {
        log.info("Création d'une nouvelle équipe : {}", request.getName());

        if (equipeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Equipe", "name", request.getName());
        }

        Equipe equipe = Equipe.builder()
                .name(request.getName())
                .pays(request.getPays())
                .entraineur(request.getEntraineur())
                .domicile(request.getDomicile() != null ? request.getDomicile() : false)
                .exterieur(request.getExterieur() != null ? request.getExterieur() : false)
                .build();

        Equipe saved = equipeRepository.save(equipe);
        log.info("Equipe créée avec id={}", saved.getId());
        return toResponse(saved);
    }

    // ──────────── UPDATE ────────────

    public EquipeResponse update(Integer id, EquipeRequest request) {
        log.info("Mise à jour de l'équipe id={}", id);

        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe", "id", id));

        if (!equipe.getName().equalsIgnoreCase(request.getName())
                && equipeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Equipe", "name", request.getName());
        }

        equipe.setName(request.getName());
        equipe.setPays(request.getPays());
        equipe.setEntraineur(request.getEntraineur());
        equipe.setDomicile(request.getDomicile() != null ? request.getDomicile() : equipe.getDomicile());
        equipe.setExterieur(request.getExterieur() != null ? request.getExterieur() : equipe.getExterieur());

        Equipe updated = equipeRepository.save(equipe);
        log.info("Equipe id={} mise à jour avec succès", id);
        return toResponse(updated);
    }

    // ──────────── DELETE ────────────

    public void delete(Integer id) {
        log.info("Suppression de l'équipe id={}", id);

        if (!equipeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipe", "id", id);
        }

        equipeRepository.deleteById(id);
        log.info("Equipe id={} supprimée", id);
    }

    // ──────────── MAPPER ────────────

    public EquipeResponse toResponse(Equipe equipe) {
        return EquipeResponse.builder()
                .id(equipe.getId())
                .name(equipe.getName())
                .pays(equipe.getPays())
                .entraineur(equipe.getEntraineur())
                .domicile(equipe.getDomicile())
                .exterieur(equipe.getExterieur())
                .build();
    }
}
