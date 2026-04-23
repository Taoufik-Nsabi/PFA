package ma.sirmaroc.matchs.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.sirmaroc.matchs.model.MatchState;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusMatchRequest {

    @Min(value = 0, message = "La duree ne peut pas etre negative")
    private Integer dureeMinutes;

    @Pattern(regexp = "^\\d+-\\d+$", message = "Le score doit respecter le format x-y")
    private String score;

    @NotNull(message = "L'etat du match est obligatoire")
    private MatchState etat;
}
