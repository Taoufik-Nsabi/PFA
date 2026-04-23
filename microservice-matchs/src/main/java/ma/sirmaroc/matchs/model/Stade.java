package ma.sirmaroc.matchs.model;

import jakarta.persistence.CascadeType;
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
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
        name = "stades",
        indexes = @Index(name = "idx_stades_api_football_id", columnList = "api_football_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le nom du stade est obligatoire")
    @Column(nullable = false, length = 150)
    private String nom;

    @NotBlank(message = "La ville est obligatoire")
    @Column(nullable = false, length = 100)
    private String ville;

    @Column(name = "api_football_id")
    private Integer apiFootballId;

    @Positive(message = "La capacite doit etre positive")
    @Column(nullable = false)
    private Integer capacite;

    @OneToMany(mappedBy = "stade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Match> matchs;
}
