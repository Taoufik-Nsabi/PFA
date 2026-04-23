package ma.sirmaroc.matchs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(
    info = @Info(
        title = "SIRMaroc - Microservice Matchs",
        version = "1.0.0",
        description = "API de gestion des matchs de la Coupe du Monde 2030 au Maroc",
        contact = @Contact(
            name = "Équipe SIRMaroc",
            email = "contact@sirmaroc.ma"
        )
    )
)
public class MicroserviceMatchsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceMatchsApplication.class, args);
    }
}
