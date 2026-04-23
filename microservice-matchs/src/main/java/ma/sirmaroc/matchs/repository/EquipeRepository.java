package ma.sirmaroc.matchs.repository;

import ma.sirmaroc.matchs.model.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Integer> {

    Optional<Equipe> findByNameIgnoreCase(String name);

    Optional<Equipe> findByApiFootballId(Integer apiFootballId);

    List<Equipe> findByPaysIgnoreCase(String pays);

    boolean existsByNameIgnoreCase(String name);
}
