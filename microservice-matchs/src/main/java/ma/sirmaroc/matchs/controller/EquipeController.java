package ma.sirmaroc.matchs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.sirmaroc.matchs.dto.ApiResponse;
import ma.sirmaroc.matchs.dto.EquipeRequest;
import ma.sirmaroc.matchs.dto.EquipeResponse;
import ma.sirmaroc.matchs.service.EquipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipes")
@RequiredArgsConstructor
@Tag(name = "Équipes", description = "API de gestion des équipes participantes")
public class EquipeController {

    private final EquipeService equipeService;

    @GetMapping
    @Operation(summary = "Lister toutes les équipes")
    public ResponseEntity<ApiResponse<List<EquipeResponse>>> findAll() {
        List<EquipeResponse> equipes = equipeService.findAll();
        return ResponseEntity.ok(ApiResponse.success(equipes, "Équipes récupérées avec succès"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une équipe par ID")
    public ResponseEntity<ApiResponse<EquipeResponse>> findById(@PathVariable Integer id) {
        EquipeResponse equipe = equipeService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(equipe, "Équipe trouvée"));
    }

    @GetMapping("/pays/{pays}")
    @Operation(summary = "Récupérer les équipes par pays")
    public ResponseEntity<ApiResponse<List<EquipeResponse>>> findByPays(@PathVariable String pays) {
        List<EquipeResponse> equipes = equipeService.findByPays(pays);
        return ResponseEntity.ok(ApiResponse.success(equipes, "Équipes trouvées pour : " + pays));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une nouvelle équipe")
    public ResponseEntity<ApiResponse<EquipeResponse>> create(@Valid @RequestBody EquipeRequest request) {
        EquipeResponse equipe = equipeService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(equipe, "Équipe créée avec succès"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour une équipe")
    public ResponseEntity<ApiResponse<EquipeResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody EquipeRequest request) {
        EquipeResponse equipe = equipeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(equipe, "Équipe mise à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une équipe")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        equipeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Équipe supprimée avec succès"));
    }
}
