package ma.sirmaroc.matchs.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class MatchResponse {
    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dateHeure;

    private StadeResponse stade;
    private EquipeResponse equipeLocale;
    private EquipeResponse equipeVisiteur;
    private StatusMatchResponse statusMatch;
}
