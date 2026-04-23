package ma.sirmaroc.matchs.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "matchs",
        indexes = @Index(name = "idx_matchs_api_football_id", columnList = "api_football_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "La date et heure du match sont obligatoires")
    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "api_football_id")
    private Integer apiFootballId;

    @NotNull(message = "Le stade est obligatoire")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stade_id", nullable = false)
    private Stade stade;

    @NotNull(message = "L'equipe locale est obligatoire")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipe_locale_id", nullable = false)
    private Equipe equipeLocale;

    @NotNull(message = "L'equipe visiteur est obligatoire")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipe_visiteur_id", nullable = false)
    private Equipe equipeVisiteur;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "status_match_id", referencedColumnName = "id")
    private StatusMatch statusMatch;
}
