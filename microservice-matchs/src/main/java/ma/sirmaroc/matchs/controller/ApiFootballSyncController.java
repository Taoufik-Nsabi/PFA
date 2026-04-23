package ma.sirmaroc.matchs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.sirmaroc.matchs.dto.ApiFootballFixturesSyncRequest;
import ma.sirmaroc.matchs.dto.ApiFootballSyncSummary;
import ma.sirmaroc.matchs.dto.ApiFootballTeamsSyncRequest;
import ma.sirmaroc.matchs.dto.ApiResponse;
import ma.sirmaroc.matchs.service.ApiFootballSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync/api-football")
@RequiredArgsConstructor
@Tag(name = "API-Football Sync", description = "Synchronisation des donnees externes API-Football")
public class ApiFootballSyncController {

    private final ApiFootballSyncService apiFootballSyncService;

    @PostMapping("/teams")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Synchroniser les equipes et stades depuis API-Football")
    public ResponseEntity<ApiResponse<ApiFootballSyncSummary>> syncTeams(
            @Valid @RequestBody ApiFootballTeamsSyncRequest request) {
        ApiFootballSyncSummary summary = apiFootballSyncService.syncTeams(request);
        return ResponseEntity.ok(ApiResponse.success(summary, "Equipes et stades synchronises avec succes"));
    }

    @PostMapping("/fixtures")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Synchroniser les matchs depuis API-Football")
    public ResponseEntity<ApiResponse<ApiFootballSyncSummary>> syncFixtures(
            @Valid @RequestBody ApiFootballFixturesSyncRequest request) {
        ApiFootballSyncSummary summary = apiFootballSyncService.syncFixtures(request);
        return ResponseEntity.ok(ApiResponse.success(summary, "Matchs synchronises avec succes"));
    }
}
