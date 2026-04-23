package ma.sirmaroc.matchs.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipeResponse {
    private Integer id;
    private String name;
    private String pays;
    private String entraineur;
    private Boolean domicile;
    private Boolean exterieur;
}
