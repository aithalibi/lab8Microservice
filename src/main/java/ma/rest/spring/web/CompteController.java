package ma.rest.spring.web;

import ma.rest.spring.entities.Compte;
import ma.rest.spring.repositories.CompteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controleur RESTful de la ressource "Compte".
 *
 * L'annotation produces/consumes declare que chaque endpoint accepte et
 * retourne du JSON ou du XML : c'est l'en-tete HTTP Accept envoye par le
 * client qui determine le format de la reponse.
 */
@RestController
@RequestMapping("/banque")
public class CompteController {

    @Autowired
    private CompteRepository compteRepository;

    // READ : recuperer la liste de tous les comptes (JSON et XML)
    @GetMapping(value = "/comptes", produces = {"application/json", "application/xml"})
    public List<Compte> getAllComptes() {
        return compteRepository.findAll();
    }

    // READ : recuperer un compte par son identifiant (JSON et XML)
    @GetMapping(value = "/comptes/{id}", produces = {"application/json", "application/xml"})
    public ResponseEntity<Compte> getCompteById(@PathVariable Long id) {
        return compteRepository.findById(id)
                .map(compte -> ResponseEntity.ok().body(compte))
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE : ajouter un nouveau compte (JSON et XML)
    @PostMapping(value = "/comptes",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    public Compte createCompte(@RequestBody Compte compte) {
        return compteRepository.save(compte);
    }

    // UPDATE : mettre a jour un compte existant (JSON et XML)
    @PutMapping(value = "/comptes/{id}",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    public ResponseEntity<Compte> updateCompte(@PathVariable Long id,
                                               @RequestBody Compte compteDetails) {
        return compteRepository.findById(id)
                .map(compte -> {
                    compte.setSolde(compteDetails.getSolde());
                    compte.setDateCreation(compteDetails.getDateCreation());
                    compte.setType(compteDetails.getType());
                    Compte updatedCompte = compteRepository.save(compte);
                    return ResponseEntity.ok().body(updatedCompte);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE : supprimer un compte
    @DeleteMapping("/comptes/{id}")
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        return compteRepository.findById(id)
                .map(compte -> {
                    compteRepository.delete(compte);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
