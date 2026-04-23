package ma.sirmaroc.matchs.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequest {

    @NotNull(message = "La date et heure du match sont obligatoires")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dateHeure;

    @NotNull(message = "L'identifiant du stade est obligatoire")
    private Integer stadeId;

    @NotNull(message = "L'identifiant de l'equipe locale est obligatoire")
    private Integer equipeLocaleId;

    @NotNull(message = "L'identifiant de l'equipe visiteur est obligatoire")
    private Integer equipeVisiteurId;

    @Valid
    private StatusMatchRequest statusMatch;
}
