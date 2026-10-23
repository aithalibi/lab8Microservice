package ma.rest.spring.repositories;

import ma.rest.spring.entities.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Depot JPA : les operations CRUD (findAll, findById, save, delete...) sont
 * generees automatiquement par Spring Data.
 */
@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {
}
