package ma.sirmaroc.matchs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "status_matchs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Min(value = 0, message = "La duree ne peut pas etre negative")
    @Column(name = "duree_minutes")
    private Integer dureeMinutes;

    @Column(length = 20)
    private String score;

    @NotNull(message = "L'etat du match est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false, length = 20)
    @Builder.Default
    private MatchState etat = MatchState.PROGRAMME;

    @OneToOne(mappedBy = "statusMatch")
    private Match match;
}
