package ma.sirmaroc.matchs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.sirmaroc.matchs.dto.ApiResponse;
import ma.sirmaroc.matchs.dto.MatchRequest;
import ma.sirmaroc.matchs.dto.MatchResponse;
import ma.sirmaroc.matchs.dto.StatusMatchRequest;
import ma.sirmaroc.matchs.service.MatchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/matchs")
@RequiredArgsConstructor
@Tag(name = "Matchs", description = "API de gestion des matchs de la Coupe du Monde 2030")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    @Operation(summary = "Lister tous les matchs")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(matchService.findAll(), "Matchs recuperes avec succes"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un match par ID")
    public ResponseEntity<ApiResponse<MatchResponse>> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(matchService.findById(id), "Match trouve"));
    }

    @GetMapping("/ville/{ville}")
    @Operation(summary = "Matchs par ville")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findByVille(@PathVariable String ville) {
        return ResponseEntity.ok(ApiResponse.success(matchService.findByVille(ville),
                "Matchs de la ville : " + ville));
    }

    @GetMapping("/stade/{stadeId}")
    @Operation(summary = "Matchs par stade")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findByStade(@PathVariable Integer stadeId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.findByStade(stadeId),
                "Matchs du stade id=" + stadeId));
    }

    @GetMapping("/equipe/{equipeId}")
    @Operation(summary = "Matchs d'une equipe (domicile ou exterieur)")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findByEquipe(@PathVariable Integer equipeId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.findByEquipe(equipeId),
                "Matchs de l'equipe id=" + equipeId));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Matchs par date (ex: 2030-06-15)")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(matchService.findByDate(date),
                "Matchs du : " + date));
    }

    @GetMapping("/status/en-cours")
    @Operation(summary = "Matchs en cours")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findEnCours() {
        return ResponseEntity.ok(ApiResponse.success(matchService.findEnCours(), "Matchs en cours"));
    }

    @GetMapping("/status/a-venir")
    @Operation(summary = "Matchs a venir")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findUpcoming() {
        return ResponseEntity.ok(ApiResponse.success(matchService.findUpcoming(), "Matchs a venir"));
    }

    @GetMapping("/status/termines")
    @Operation(summary = "Matchs termines")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findTermines() {
        return ResponseEntity.ok(ApiResponse.success(matchService.findTermines(), "Matchs termines"));
    }

    @GetMapping("/status/reportes")
    @Operation(summary = "Matchs reportes")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findReportes() {
        return ResponseEntity.ok(ApiResponse.success(matchService.findReportes(), "Matchs reportes"));
    }

    @GetMapping("/status/suspendus")
    @Operation(summary = "Matchs suspendus")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> findSuspendus() {
        return ResponseEntity.ok(ApiResponse.success(matchService.findSuspendus(), "Matchs suspendus"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Creer un nouveau match")
    public ResponseEntity<ApiResponse<MatchResponse>> create(@Valid @RequestBody MatchRequest request) {
        MatchResponse match = matchService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(match, "Match cree avec succes"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre a jour un match")
    public ResponseEntity<ApiResponse<MatchResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody MatchRequest request) {
        MatchResponse match = matchService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(match, "Match mis a jour avec succes"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre a jour uniquement le statut d'un match")
    public ResponseEntity<ApiResponse<MatchResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusMatchRequest statusRequest) {
        MatchResponse match = matchService.updateStatus(id, statusRequest);
        return ResponseEntity.ok(ApiResponse.success(match, "Statut du match mis a jour"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un match")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        matchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Match supprime avec succes"));
    }
}
