package ma.sirmaroc.matchs.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.sirmaroc.matchs.model.Equipe;
import ma.sirmaroc.matchs.model.Match;
import ma.sirmaroc.matchs.model.MatchState;
import ma.sirmaroc.matchs.model.Stade;
import ma.sirmaroc.matchs.model.StatusMatch;
import ma.sirmaroc.matchs.repository.EquipeRepository;
import ma.sirmaroc.matchs.repository.MatchRepository;
import ma.sirmaroc.matchs.repository.StadeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    @Profile("dev")
    public CommandLineRunner initData(
            StadeRepository stadeRepository,
            EquipeRepository equipeRepository,
            MatchRepository matchRepository) {

        return args -> {
            if (stadeRepository.count() > 0) {
                log.info("Donnees deja initialisees, skip.");
                return;
            }

            log.info("Initialisation des donnees de demonstration...");

            Stade stadeMarrakech = stadeRepository.save(
                Stade.builder()
                    .nom("Stade de Marrakech")
                    .ville("Marrakech")
                    .capacite(45240)
                    .build()
            );

            Stade stadeCasablanca = stadeRepository.save(
                Stade.builder()
                    .nom("Grand Stade de Casablanca")
                    .ville("Casablanca")
                    .capacite(93000)
                    .build()
            );

            Stade stadeRabat = stadeRepository.save(
                Stade.builder()
                    .nom("Stade Prince Moulay Abdellah")
                    .ville("Rabat")
                    .capacite(52000)
                    .build()
            );

            log.info("3 stades crees.");

            Equipe maroc = equipeRepository.save(
                Equipe.builder()
                    .name("Maroc")
                    .pays("Maroc")
                    .entraineur("Walid Regragui")
                    .domicile(true)
                    .exterieur(false)
                    .build()
            );

            Equipe france = equipeRepository.save(
                Equipe.builder()
                    .name("France")
                    .pays("France")
                    .entraineur("Didier Deschamps")
                    .domicile(false)
                    .exterieur(true)
                    .build()
            );

            Equipe bresil = equipeRepository.save(
                Equipe.builder()
                    .name("Bresil")
                    .pays("Bresil")
                    .entraineur("Carlo Ancelotti")
                    .domicile(false)
                    .exterieur(true)
                    .build()
            );

            Equipe espagne = equipeRepository.save(
                Equipe.builder()
                    .name("Espagne")
                    .pays("Espagne")
                    .entraineur("Luis de la Fuente")
                    .domicile(false)
                    .exterieur(true)
                    .build()
            );

            log.info("4 equipes creees.");

            StatusMatch status1 = StatusMatch.builder()
                    .dureeMinutes(0)
                    .score("0-0")
                    .etat(MatchState.PROGRAMME)
                    .build();

            matchRepository.save(
                Match.builder()
                    .dateHeure(LocalDateTime.of(2030, 6, 15, 18, 0))
                    .stade(stadeCasablanca)
                    .equipeLocale(maroc)
                    .equipeVisiteur(france)
                    .statusMatch(status1)
                    .build()
            );

            StatusMatch status2 = StatusMatch.builder()
                    .dureeMinutes(0)
                    .score("0-0")
                    .etat(MatchState.PROGRAMME)
                    .build();

            matchRepository.save(
                Match.builder()
                    .dateHeure(LocalDateTime.of(2030, 6, 16, 20, 0))
                    .stade(stadeMarrakech)
                    .equipeLocale(bresil)
                    .equipeVisiteur(espagne)
                    .statusMatch(status2)
                    .build()
            );

            StatusMatch status3 = StatusMatch.builder()
                    .dureeMinutes(90)
                    .score("2-1")
                    .etat(MatchState.TERMINE)
                    .build();

            matchRepository.save(
                Match.builder()
                    .dateHeure(LocalDateTime.of(2030, 6, 10, 18, 0))
                    .stade(stadeRabat)
                    .equipeLocale(espagne)
                    .equipeVisiteur(maroc)
                    .statusMatch(status3)
                    .build()
            );

            log.info("3 matchs crees. Initialisation terminee.");
        };
    }
}
