package ma.sirmaroc.matchs.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiFootballFixturesSyncRequest {

    @NotNull(message = "L'identifiant de la ligue est obligatoire")
    private Integer leagueId;

    @NotNull(message = "La saison est obligatoire")
    private Integer season;

    @NotNull(message = "La date de debut est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @NotNull(message = "La date de fin est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @Builder.Default
    private Boolean syncTeamsBeforeFixtures = true;

    @AssertTrue(message = "La date de debut doit etre inferieure ou egale a la date de fin")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }
}
