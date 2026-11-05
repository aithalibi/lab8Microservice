package ma.rest.spring.repositories;

import ma.rest.spring.entities.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Etape 5 du TP 8 : depot Spring Data JPA, aucune implementation a ecrire.
 */
@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {
}
