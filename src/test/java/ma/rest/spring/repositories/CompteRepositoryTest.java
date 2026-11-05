package ma.rest.spring.repositories;

import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification de l'etape 5 (depot JPA) et de l'etape 3 (mapping de l'entite).
 * {@link DataJpaTest} demarre uniquement la tranche JPA sur H2.
 */
@DataJpaTest
class CompteRepositoryTest {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void clean() {
        compteRepository.deleteAll();
    }

    @Test
    @DisplayName("save() genere un identifiant pour un compte neuf")
    void saveGeneratesId() {
        Compte saved = compteRepository.save(new Compte(null, 1500.0, new Date(), TypeCompte.COURANT));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSolde()).isEqualTo(1500.0);
        assertThat(saved.getType()).isEqualTo(TypeCompte.COURANT);
    }

    @Test
    @DisplayName("findAll() renvoie tous les comptes enregistres")
    void findAllReturnsEveryAccount() {
        compteRepository.save(new Compte(null, 100.0, new Date(), TypeCompte.COURANT));
        compteRepository.save(new Compte(null, 200.0, new Date(), TypeCompte.EPARGNE));

        List<Compte> comptes = compteRepository.findAll();

        assertThat(comptes).hasSize(2);
        assertThat(comptes).extracting(Compte::getSolde).containsExactlyInAnyOrder(100.0, 200.0);
    }

    @Test
    @DisplayName("findById() renvoie le compte attendu")
    void findByIdReturnsAccount() {
        Compte saved = compteRepository.save(new Compte(null, 42.0, new Date(), TypeCompte.EPARGNE));

        Optional<Compte> found = compteRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSolde()).isEqualTo(42.0);
        assertThat(found.get().getType()).isEqualTo(TypeCompte.EPARGNE);
    }

    @Test
    @DisplayName("findById() renvoie un Optional vide pour un identifiant inconnu")
    void findByIdUnknownReturnsEmpty() {
        assertThat(compteRepository.findById(999_999L)).isEmpty();
    }

    @Test
    @DisplayName("deleteById() supprime le compte")
    void deleteByIdRemovesAccount() {
        Compte saved = compteRepository.save(new Compte(null, 77.0, new Date(), TypeCompte.COURANT));

        compteRepository.deleteById(saved.getId());

        assertThat(compteRepository.findById(saved.getId())).isEmpty();
        assertThat(compteRepository.count()).isZero();
    }

    @Test
    @DisplayName("@Enumerated(STRING) stocke le libelle de l'enumeration, pas l'ordinal")
    void enumerationIsStoredAsString() {
        Compte saved = compteRepository.save(new Compte(null, 500.0, new Date(), TypeCompte.EPARGNE));
        entityManager.flush();
        entityManager.clear();

        Object stored = entityManager.getEntityManager()
                .createNativeQuery("SELECT type FROM compte WHERE id = ?1")
                .setParameter(1, saved.getId())
                .getSingleResult();

        assertThat(stored.toString()).isEqualTo("EPARGNE");
    }
}
