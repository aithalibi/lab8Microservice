package ma.rest.spring.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du controleur REST : verification du support JSON et XML.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CompteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getComptesEnJson() throws Exception {
        mockMvc.perform(get("/banque/comptes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").exists());
    }

    @Test
    void getComptesEnXml() throws Exception {
        // Une List<Compte> est serialisee en <List><item>...</item></List>
        mockMvc.perform(get("/banque/comptes").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<List>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<item>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<type>")));
    }

    @Test
    void getCompteByIdEnXml() throws Exception {
        // Un Compte seul est serialise avec la racine <compte> (@JacksonXmlRootElement)
        mockMvc.perform(get("/banque/comptes/1").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<compte>")));
    }

    @Test
    void createCompteEnJson() throws Exception {
        String body = "{\"solde\": 1500.0, \"dateCreation\": \"2024-11-01\", \"type\": \"COURANT\"}";

        mockMvc.perform(post("/banque/comptes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("COURANT"))
                .andExpect(jsonPath("$.dateCreation").value("2024-11-01"));
    }

    @Test
    void getCompteInexistantRetourne404() throws Exception {
        mockMvc.perform(get("/banque/comptes/999999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
