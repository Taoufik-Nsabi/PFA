package ma.sirmaroc.matchs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class StadeRequest {

    @NotBlank(message = "Le nom du stade est obligatoire")
    private String nom;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotNull(message = "La capacite du stade est obligatoire")
    @Positive(message = "La capacite doit etre un nombre positif")
    private Integer capacite;
}
