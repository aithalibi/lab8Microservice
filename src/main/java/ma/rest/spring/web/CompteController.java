package ma.rest.spring.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.rest.spring.entities.Compte;
import ma.rest.spring.repositories.CompteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Etapes 7 et 9 du TP 8 : service REST Spring MVC sur {@code /banque/comptes}.
 *
 * <p>Chaque methode declare {@code produces} (et {@code consumes} pour les
 * ecritures) avec les deux types de media : Spring choisit le format reellement
 * renvoye d'apres l'en-tete {@code Accept} du client, via
 * {@code MappingJackson2XmlHttpMessageConverter} pour le XML.</p>
 */
@RestController
@RequestMapping("/banque")
@Tag(name = "Comptes", description = "Gestion des comptes bancaires (JSON et XML)")
public class CompteController {

    private final CompteRepository compteRepository;

    public CompteController(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    @Operation(summary = "Lister tous les comptes")
    @GetMapping(value = "/comptes", produces = {"application/json", "application/xml"})
    public List<Compte> getAllComptes() {
        return compteRepository.findAll();
    }

    @Operation(summary = "Consulter un compte par son identifiant")
    @ApiResponse(responseCode = "200", description = "Compte trouve")
    @ApiResponse(responseCode = "404", description = "Aucun compte pour cet identifiant")
    @GetMapping(value = "/comptes/{id}", produces = {"application/json", "application/xml"})
    public ResponseEntity<Compte> getCompteById(@PathVariable Long id) {
        return compteRepository.findById(id)
                .map(compte -> ResponseEntity.ok().body(compte))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Creer un compte")
    @ApiResponse(responseCode = "201", description = "Compte cree, l'URI est renvoyee dans l'en-tete Location")
    @PostMapping(value = "/comptes",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    public ResponseEntity<Compte> createCompte(@RequestBody Compte compte) {
        Compte savedCompte = compteRepository.save(compte);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCompte.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedCompte);
    }

    @Operation(summary = "Mettre a jour un compte existant")
    @ApiResponse(responseCode = "200", description = "Compte mis a jour")
    @ApiResponse(responseCode = "404", description = "Aucun compte pour cet identifiant")
    @PutMapping(value = "/comptes/{id}",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    public ResponseEntity<Compte> updateCompte(@PathVariable Long id, @RequestBody Compte compteDetails) {
        return compteRepository.findById(id)
                .map(compte -> {
                    compte.setSolde(compteDetails.getSolde());
                    compte.setDateCreation(compteDetails.getDateCreation());
                    compte.setType(compteDetails.getType());
                    Compte updatedCompte = compteRepository.save(compte);
                    return ResponseEntity.ok().body(updatedCompte);
                }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Supprimer un compte")
    @DeleteMapping("/comptes/{id}")
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        return compteRepository.findById(id)
                .map(compte -> {
                    compteRepository.delete(compte);
                    return ResponseEntity.ok().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }
}
