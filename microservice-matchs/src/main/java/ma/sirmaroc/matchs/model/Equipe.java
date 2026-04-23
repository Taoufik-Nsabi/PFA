package ma.sirmaroc.matchs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
        name = "equipes",
        indexes = @Index(name = "idx_equipes_api_football_id", columnList = "api_football_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le nom de l'equipe est obligatoire")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Le pays est obligatoire")
    @Column(nullable = false, length = 100)
    private String pays;

    @Column(name = "api_football_id")
    private Integer apiFootballId;

    @Column(length = 150)
    private String entraineur;

    @Column(nullable = false)
    @Builder.Default
    private Boolean domicile = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean exterieur = false;

    @OneToMany(mappedBy = "equipeLocale", fetch = FetchType.LAZY)
    private List<Match> matchsLocaux;

    @OneToMany(mappedBy = "equipeVisiteur", fetch = FetchType.LAZY)
    private List<Match> matchsVisiteur;
}
