package ma.rest.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autorise le client React du TP 9 (dev server CRA sur le port 3000) a appeler
 * l'API depuis le navigateur.
 *
 * <p>Le navigateur envoie une requete preflight OPTIONS avant un POST JSON /
 * XML : sans cet enregistrement, le client recoit une erreur CORS et l'ajout de
 * compte echoue alors que curl fonctionne. Les origines autorisees sont limitees
 * aux ports de developpement locaux plutot qu'a {@code "*"}.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/banque/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept", "Origin", "X-Requested-With")
                .maxAge(3600);
    }
}
