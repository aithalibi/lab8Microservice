package ma.rest.spring.web;

import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import ma.rest.spring.repositories.CompteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification des etapes 7, 9 et 11 du TP 8 : les cinq operations CRUD sont
 * appelees en HTTP reel, en JSON puis en XML, pour valider la negociation de
 * contenu et les codes de statut.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompteControllerTest {

    private static final String BASE = "/banque/comptes";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompteRepository compteRepository;

    @BeforeEach
    void clean() {
        compteRepository.deleteAll();
    }

    private Compte sample() {
        return new Compte(null, 1500.0, new Date(), TypeCompte.COURANT);
    }

    private HttpHeaders accept(MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(mediaType));
        return headers;
    }

    private HttpHeaders contentType(MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setAccept(List.of(mediaType));
        return headers;
    }

    @Test
    @DisplayName("GET /banque/comptes renvoie la liste en JSON")
    void getAllComptesAsJson() {
        compteRepository.save(sample());

        ResponseEntity<String> response = restTemplate.exchange(BASE, HttpMethod.GET,
                new HttpEntity<>(accept(MediaType.APPLICATION_JSON)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).contains("\"solde\":1500.0").contains("\"type\":\"COURANT\"");
    }

    @Test
    @DisplayName("GET /banque/comptes renvoie la liste en XML")
    void getAllComptesAsXml() {
        compteRepository.save(sample());

        ResponseEntity<String> response = restTemplate.exchange(BASE, HttpMethod.GET,
                new HttpEntity<>(accept(MediaType.APPLICATION_XML)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_XML);
        // Jackson nomme l'element racine d'apres le type declare (List) et chaque
        // entree "item" : c'est le rendu XML par defaut du code de l'enonce.
        assertThat(response.getBody())
                .contains("<List>")
                .contains("<item>")
                .contains("<type>COURANT</type>");
    }

    @Test
    @DisplayName("GET /banque/comptes/{id} renvoie 200 et le compte en JSON")
    void getCompteByIdAsJson() {
        Compte saved = compteRepository.save(sample());

        ResponseEntity<String> response = restTemplate.exchange(BASE + "/" + saved.getId(), HttpMethod.GET,
                new HttpEntity<>(accept(MediaType.APPLICATION_JSON)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"id\":" + saved.getId());
    }

    @Test
    @DisplayName("GET /banque/comptes/{id} renvoie 404 pour un identifiant inconnu")
    void getCompteByIdUnknownReturnsNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(BASE + "/999999", HttpMethod.GET,
                new HttpEntity<>(accept(MediaType.APPLICATION_JSON)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /banque/comptes cree un compte en JSON et renvoie 201 + Location")
    void createCompteAsJson() {
        ResponseEntity<Compte> response = restTemplate.exchange(BASE, HttpMethod.POST,
                new HttpEntity<>(sample(), contentType(MediaType.APPLICATION_JSON)), Compte.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo(TypeCompte.COURANT);
        assertThat(compteRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /banque/comptes cree un compte en XML et renvoie 201")
    void createCompteAsXml() {
        String xml = "<compte><solde>1500.0</solde><dateCreation>2024-11-01</dateCreation>"
                + "<type>COURANT</type></compte>";

        ResponseEntity<String> response = restTemplate.exchange(BASE, HttpMethod.POST,
                new HttpEntity<>(xml, contentType(MediaType.APPLICATION_XML)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_XML);
        assertThat(response.getBody()).contains("<type>COURANT</type>");
        assertThat(compteRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("PUT /banque/comptes/{id} met a jour le compte en JSON")
    void updateCompteAsJson() {
        Compte saved = compteRepository.save(sample());
        Compte update = new Compte(null, 2000.0, new Date(), TypeCompte.EPARGNE);

        ResponseEntity<Compte> response = restTemplate.exchange(BASE + "/" + saved.getId(), HttpMethod.PUT,
                new HttpEntity<>(update, contentType(MediaType.APPLICATION_JSON)), Compte.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSolde()).isEqualTo(2000.0);
        assertThat(response.getBody().getType()).isEqualTo(TypeCompte.EPARGNE);
        assertThat(compteRepository.findById(saved.getId()).orElseThrow().getSolde()).isEqualTo(2000.0);
    }

    @Test
    @DisplayName("PUT /banque/comptes/{id} renvoie 404 pour un identifiant inconnu")
    void updateCompteUnknownReturnsNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(BASE + "/999999", HttpMethod.PUT,
                new HttpEntity<>(sample(), contentType(MediaType.APPLICATION_JSON)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /banque/comptes/{id} supprime le compte puis renvoie 404")
    void deleteCompteThenNotFound() {
        Compte saved = compteRepository.save(sample());

        ResponseEntity<Void> deletion = restTemplate.exchange(BASE + "/" + saved.getId(), HttpMethod.DELETE,
                new HttpEntity<>(accept(MediaType.APPLICATION_JSON)), Void.class);

        assertThat(deletion.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(compteRepository.count()).isZero();

        ResponseEntity<Void> secondCall = restTemplate.exchange(BASE + "/" + saved.getId(), HttpMethod.DELETE,
                new HttpEntity<>(accept(MediaType.APPLICATION_JSON)), Void.class);

        assertThat(secondCall.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST avec un corps JSON invalide renvoie 400 et un message clair")
    void createWithMalformedJsonReturnsBadRequest() {
        ResponseEntity<String> response = restTemplate.exchange(BASE, HttpMethod.POST,
                new HttpEntity<>("{\"solde\": \"pas-un-nombre\"}", contentType(MediaType.APPLICATION_JSON)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Corps de requete illisible");
        assertThat(compteRepository.count()).isZero();
    }

    @Test
    @DisplayName("GET avec un identifiant non numerique renvoie 400")
    void getWithNonNumericIdReturnsBadRequest() {
        ResponseEntity<String> response = restTemplate.exchange(BASE + "/abc", HttpMethod.GET,
                new HttpEntity<>(accept(MediaType.APPLICATION_JSON)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Parametre 'id' invalide");
    }

    @Test
    @DisplayName("Sans en-tete Accept, Spring renvoie du JSON par defaut")
    void defaultContentTypeIsJson() {
        compteRepository.save(sample());

        ResponseEntity<String> response = restTemplate.getForEntity(BASE, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).startsWith("[").contains("\"type\":\"COURANT\"");
    }

    @Test
    @DisplayName("Un media type non supporte renvoie 406")
    void unsupportedAcceptReturnsNotAcceptable() {
        compteRepository.save(sample());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.TEXT_HTML));

        ResponseEntity<String> response = restTemplate.exchange(BASE, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }
}
