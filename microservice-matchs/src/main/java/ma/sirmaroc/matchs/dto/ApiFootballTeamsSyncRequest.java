package ma.sirmaroc.matchs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiFootballTeamsSyncRequest {

    @NotNull(message = "L'identifiant de la ligue est obligatoire")
    private Integer leagueId;

    @NotNull(message = "La saison est obligatoire")
    private Integer season;
}
