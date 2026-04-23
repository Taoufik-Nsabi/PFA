package ma.sirmaroc.matchs.dto;

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
public class StatusMatchResponse {
    private Integer id;
    private Integer dureeMinutes;
    private String score;
    private MatchState etat;
}
