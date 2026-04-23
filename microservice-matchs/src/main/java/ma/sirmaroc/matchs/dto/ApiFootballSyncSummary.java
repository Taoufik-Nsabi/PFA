package ma.sirmaroc.matchs.dto;

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
public class ApiFootballSyncSummary {

    private Integer leagueId;
    private Integer season;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int teamsProcessed;
    private int teamsCreated;
    private int teamsUpdated;
    private int stadesProcessed;
    private int stadesCreated;
    private int stadesUpdated;
    private int matchesProcessed;
    private int matchesCreated;
    private int matchesUpdated;
    private boolean syncedTeamsBeforeFixtures;

    public void incrementTeamsProcessed() {
        teamsProcessed++;
    }

    public void incrementTeamsCreated() {
        teamsCreated++;
    }

    public void incrementTeamsUpdated() {
        teamsUpdated++;
    }

    public void incrementStadesProcessed() {
        stadesProcessed++;
    }

    public void incrementStadesCreated() {
        stadesCreated++;
    }

    public void incrementStadesUpdated() {
        stadesUpdated++;
    }

    public void incrementMatchesProcessed() {
        matchesProcessed++;
    }

    public void incrementMatchesCreated() {
        matchesCreated++;
    }

    public void incrementMatchesUpdated() {
        matchesUpdated++;
    }

    public void merge(ApiFootballSyncSummary other) {
        teamsProcessed += other.teamsProcessed;
        teamsCreated += other.teamsCreated;
        teamsUpdated += other.teamsUpdated;
        stadesProcessed += other.stadesProcessed;
        stadesCreated += other.stadesCreated;
        stadesUpdated += other.stadesUpdated;
        matchesProcessed += other.matchesProcessed;
        matchesCreated += other.matchesCreated;
        matchesUpdated += other.matchesUpdated;
    }
}
