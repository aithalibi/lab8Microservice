package ma.rest.spring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Etape 8 du TP 8 : personnalisation de la documentation OpenAPI generee par
 * springdoc. Sans ce bean, Swagger UI affiche le titre par defaut
 * « OpenAPI definition ».
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI banqueOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Banque - API REST")
                        .version("1.0.0")
                        .description("""
                                Service bancaire du TP 8 (Spring @RestController).

                                Les cinq operations CRUD sont disponibles en JSON et en XML :
                                le format renvoye depend de l'en-tete `Accept` de la requete.
                                """)
                        .contact(new Contact().name("Yasmine Ait Halibi"))
                        .license(new License().name("MIT")));
    }
}
