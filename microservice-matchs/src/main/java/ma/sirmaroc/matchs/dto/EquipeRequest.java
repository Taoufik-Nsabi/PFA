package ma.sirmaroc.matchs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipeRequest {

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    private String name;

    @NotBlank(message = "Le pays est obligatoire")
    private String pays;

    private String entraineur;
    @Builder.Default
    private Boolean domicile = false;
    @Builder.Default
    private Boolean exterieur = false;
}
