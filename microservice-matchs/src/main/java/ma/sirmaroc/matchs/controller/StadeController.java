package ma.sirmaroc.matchs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.sirmaroc.matchs.dto.ApiResponse;
import ma.sirmaroc.matchs.dto.StadeRequest;
import ma.sirmaroc.matchs.dto.StadeResponse;
import ma.sirmaroc.matchs.service.StadeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stades")
@RequiredArgsConstructor
@Tag(name = "Stades", description = "API de gestion des stades")
public class StadeController {

    private final StadeService stadeService;

    @GetMapping
    @Operation(summary = "Lister tous les stades")
    public ResponseEntity<ApiResponse<List<StadeResponse>>> findAll() {
        List<StadeResponse> stades = stadeService.findAll();
        return ResponseEntity.ok(ApiResponse.success(stades, "Stades récupérés avec succès"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un stade par ID")
    public ResponseEntity<ApiResponse<StadeResponse>> findById(@PathVariable Integer id) {
        StadeResponse stade = stadeService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(stade, "Stade trouvé"));
    }

    @GetMapping("/ville/{ville}")
    @Operation(summary = "Récupérer les stades par ville")
    public ResponseEntity<ApiResponse<List<StadeResponse>>> findByVille(@PathVariable String ville) {
        List<StadeResponse> stades = stadeService.findByVille(ville);
        return ResponseEntity.ok(ApiResponse.success(stades, "Stades trouvés pour la ville : " + ville));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un nouveau stade")
    public ResponseEntity<ApiResponse<StadeResponse>> create(@Valid @RequestBody StadeRequest request) {
        StadeResponse stade = stadeService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(stade, "Stade créé avec succès"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un stade")
    public ResponseEntity<ApiResponse<StadeResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody StadeRequest request) {
        StadeResponse stade = stadeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(stade, "Stade mis à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un stade")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        stadeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Stade supprimé avec succès"));
    }
}
