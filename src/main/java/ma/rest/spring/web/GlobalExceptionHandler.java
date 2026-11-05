package ma.rest.spring.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Gestion centralisee des erreurs de requete (mentionnee a l'etape 12 du TP).
 *
 * <p>On se limite aux erreurs de <em>format</em> : un corps JSON/XML illisible ou
 * un identifiant non numerique renvoient 400 avec un message explicite au lieu
 * d'une trace serveur. Les autres exceptions gardent le comportement par defaut
 * de Spring Boot (dont le 404 des chemins inconnus).</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Corps de requete illisible : verifiez la syntaxe JSON/XML et les valeurs des champs.");
        problem.setTitle("Requete invalide");
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Parametre '" + exception.getName() + "' invalide : " + exception.getValue());
        problem.setTitle("Parametre invalide");
        return problem;
    }
}
