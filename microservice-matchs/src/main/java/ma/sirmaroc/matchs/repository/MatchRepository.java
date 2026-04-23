package ma.sirmaroc.matchs.repository;

import ma.sirmaroc.matchs.model.Match;
import ma.sirmaroc.matchs.model.MatchState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {

    List<Match> findByStadeId(Integer stadeId);

    Optional<Match> findByApiFootballId(Integer apiFootballId);

    @Query("SELECT m FROM Match m WHERE LOWER(m.stade.ville) = LOWER(:ville)")
    List<Match> findByVille(@Param("ville") String ville);

    @Query("SELECT m FROM Match m WHERE m.equipeLocale.id = :equipeId OR m.equipeVisiteur.id = :equipeId")
    List<Match> findByEquipeId(@Param("equipeId") Integer equipeId);

    @Query("""
            SELECT m
            FROM Match m
            WHERE m.dateHeure >= :startOfDay
              AND m.dateHeure < :endOfDay
            """)
    List<Match> findByDate(@Param("startOfDay") LocalDateTime startOfDay,
                           @Param("endOfDay") LocalDateTime endOfDay);

    List<Match> findByStatusMatchEtat(MatchState etat);
}
