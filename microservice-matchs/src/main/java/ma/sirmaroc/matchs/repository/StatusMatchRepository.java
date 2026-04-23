package ma.sirmaroc.matchs.repository;

import ma.sirmaroc.matchs.model.MatchState;
import ma.sirmaroc.matchs.model.StatusMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusMatchRepository extends JpaRepository<StatusMatch, Integer> {

    List<StatusMatch> findByEtat(MatchState etat);
}
