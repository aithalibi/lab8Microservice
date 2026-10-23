package ma.rest.spring;

import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import ma.rest.spring.repositories.CompteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

/**
 * TP 8 : Spring @RestController
 *
 * Application Spring Boot exposant un service REST (JSON + XML) sur la ressource
 * "Compte", avec persistance H2 et documentation Swagger (springdoc-openapi).
 */
@SpringBootApplication
public class MsBanqueApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsBanqueApplication.class, args);
    }

    /**
     * Initialisation de quelques comptes au demarrage de l'application.
     */
    @Bean
    CommandLineRunner start(CompteRepository compteRepository) {
        return args -> {
            compteRepository.save(new Compte(null, Math.random() * 9000, LocalDate.now(), TypeCompte.EPARGNE));
            compteRepository.save(new Compte(null, Math.random() * 9000, LocalDate.now(), TypeCompte.COURANT));
            compteRepository.save(new Compte(null, Math.random() * 9000, LocalDate.now(), TypeCompte.EPARGNE));

            System.out.println("----- Comptes initialises -----");
            compteRepository.findAll().forEach(c -> System.out.println(c.toString()));
            System.out.println("-------------------------------");
        };
    }
}
