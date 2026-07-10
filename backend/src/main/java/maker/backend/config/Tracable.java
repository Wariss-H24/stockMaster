package maker.backend.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer une méthode à tracer dans l'audit log.
 * Exemple : @Tracable(entite = "Produit", action = "CREER")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tracable {
    String entite();
    String action();
}
