package maker.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité — tout ouvert en développement.
 * À remplacer par JWT (Spring Security + filter chain) au module auth.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF pour l'API REST stateless
            .csrf(csrf -> csrf.disable())
            // Tout autoriser (dev) — à restreindre avec les rôles plus tard
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // Autoriser les frames H2 console
            .headers(headers -> headers.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }
}
