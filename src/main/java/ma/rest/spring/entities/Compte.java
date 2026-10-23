package ma.rest.spring.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entite JPA representant un compte bancaire.
 *
 * @JacksonXmlRootElement permet d'avoir la racine &lt;compte&gt; en XML
 * (utilise par les requetes curl en application/xml).
 *
 * Remarque : le support de date utilise java.time.LocalDate au lieu de
 * java.util.Date + @Temporal(TemporalType.DATE) comme dans l'enonce. Avec
 * Hibernate 6 (Spring Boot 3), java.sql.Date provoque un decalage de fuseau
 * horaire a la relecture (2024-12-15 devient 2024-12-14). Le contrat REST
 * reste identique : "yyyy-MM-dd".
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "compte")
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double solde;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateCreation;

    @Enumerated(EnumType.STRING)
    private TypeCompte type;
}
