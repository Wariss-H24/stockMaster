package maker.backend.config;

import maker.backend.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration Spring Security avec JWT stateless et restrictions par rôle.
 *
 * Permissions :
 *  - /api/auth/**          → public (login, register)
 *  - /h2-console/**        → public (dev)
 *  - GET  /api/**          → ADMIN, GESTIONNAIRE, MAGASINIER, AUDITEUR
 *  - POST/PUT /api/**      → ADMIN, GESTIONNAIRE
 *  - DELETE /api/**        → ADMIN uniquement
 *  - /api/utilisateurs/**  → ADMIN uniquement
 */
@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    /** Encodeur BCrypt pour les mots de passe */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Provider d'authentification : UserDetailsService + BCrypt */
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    /** AuthenticationManager exposé pour AuthService */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // API stateless : pas de session HTTP
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 console
            .authorizeHttpRequests(auth -> auth

                // --- Routes publiques ---
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // --- Gestion des utilisateurs : ADMIN seulement ---
                .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN")

                // --- Lecture : tous les rôles connectés (ou authentifiés pour stocks/mouvements) ---
                .requestMatchers(HttpMethod.GET, "/api/stocks/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/mouvements-stock/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR")

                // --- Création et modification : ADMIN + GESTIONNAIRE ---
                .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "GESTIONNAIRE")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "GESTIONNAIRE")

                // --- Suppression / désactivation : ADMIN seulement, sauf bons en brouillon pour GESTIONNAIRE
                .requestMatchers(HttpMethod.DELETE, "/api/bon-receptions/**").hasAnyRole("ADMIN", "GESTIONNAIRE")
                .requestMatchers(HttpMethod.DELETE, "/api/bon-sorties/**").hasAnyRole("ADMIN", "GESTIONNAIRE")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                // Tout le reste : authentifié
                .anyRequest().authenticated()
            )
            .authenticationProvider(authProvider())
            // Injecter le filtre JWT avant le filtre d'auth par défaut
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
