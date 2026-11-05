package ma.rest.spring.config;

import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import ma.rest.spring.repositories.CompteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verification du TP 9 (integration) : le serveur de developpement React tourne
 * sur le port 3000 et appelle l'API du port 8082. Sans les en-tetes CORS le
 * navigateur bloque la requete ; MockMvc permet de controler l'en-tete
 * {@code Origin}, ce que {@code TestRestTemplate} ne peut pas faire car
 * {@code HttpURLConnection} supprime les en-tetes restreints.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    private static final String BASE = "/banque/comptes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompteRepository compteRepository;

    @BeforeEach
    void clean() {
        compteRepository.deleteAll();
        compteRepository.save(new Compte(null, 1500.0, new Date(), TypeCompte.COURANT));
    }

    @Test
    @DisplayName("Le preflight OPTIONS du client React obtient les en-tetes CORS")
    void preflightFromReactDevServerIsAllowed() throws Exception {
        mockMvc.perform(options(BASE)
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    @DisplayName("Un GET reel depuis le client React porte l'en-tete Access-Control-Allow-Origin")
    void realGetFromReactDevServerCarriesCorsHeader() throws Exception {
        mockMvc.perform(get(BASE)
                        .header("Origin", "http://localhost:3000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("Une origine non autorisee ne recoit pas d'en-tete CORS")
    void unknownOriginIsRejected() throws Exception {
        mockMvc.perform(options(BASE)
                        .header("Origin", "http://example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
