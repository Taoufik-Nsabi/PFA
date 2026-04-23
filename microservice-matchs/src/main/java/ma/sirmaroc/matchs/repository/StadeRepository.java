package ma.sirmaroc.matchs.repository;

import ma.sirmaroc.matchs.model.Stade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StadeRepository extends JpaRepository<Stade, Integer> {

    Optional<Stade> findByNomIgnoreCase(String nom);

    Optional<Stade> findByApiFootballId(Integer apiFootballId);

    List<Stade> findByVilleIgnoreCase(String ville);

    boolean existsByNomIgnoreCase(String nom);
}
