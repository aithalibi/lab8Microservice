package ma.rest.spring.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Etape 3 du TP 8 (entite JPA) et etape 9.c (support de la serialisation XML).
 *
 * <p>Deux adaptations par rapport au code de l'enonce :</p>
 * <ul>
 *   <li>{@code javax.persistence} devient {@code jakarta.persistence} : Spring
 *       Boot 3 s'appuie sur Jakarta EE 9+.</li>
 *   <li>{@link JacksonXmlRootElement} fixe le nom de l'element racine XML a
 *       {@code compte} (par defaut Jackson utiliserait le nom de la classe, ce
 *       qui donne le meme resultat ici, mais l'annotation rend le contrat
 *       explicite et stable en cas de renommage).</li>
 * </ul>
 */
@Entity
@Table(name = "compte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "compte")
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double solde;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Paris")
    private Date dateCreation;

    /**
     * {@link EnumType#STRING} stocke le libelle ({@code COURANT}, {@code EPARGNE})
     * au lieu de l'index ordinal : les donnees restent lisibles en base et
     * resistent a un reordonnancement de l'enumeration.
     */
    @Enumerated(EnumType.STRING)
    private TypeCompte type;
}
