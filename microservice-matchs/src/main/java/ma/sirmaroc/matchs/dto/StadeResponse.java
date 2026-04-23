package ma.sirmaroc.matchs.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StadeResponse {
    private Integer id;
    private String nom;
    private String ville;
    private Integer capacite;
}
