package maker.backend.config;

import maker.backend.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Permissions par rôle :
 *
 * ADMIN          → tout
 * GESTIONNAIRE   → tout sauf : créer entrepôt, gérer utilisateurs/rôles
 * MAGASINIER     → lecture + bons entrée/sortie + transferts. PAS : créer entrepôt/zone/rayon/étagère/emplacement/produit
 * AUDITEUR       → lecture seule
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> {})
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .authorizeHttpRequests(auth -> auth

                // ── Publiques ──────────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // ── Utilisateurs & rôles : ADMIN seulement ─────────────────
                .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN")

                // ── Entrepôts : lecture tous, création/modif ADMIN+GESTIONNAIRE, suppression ADMIN ──
                .requestMatchers(HttpMethod.GET,    "/api/entrepots/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,   "/api/entrepots").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/entrepots/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/entrepots/**").hasRole("ADMIN")

                // ── Zones : lecture tous, CRUD ADMIN+GESTIONNAIRE ──────────
                .requestMatchers(HttpMethod.GET,    "/api/zones/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,   "/api/zones").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.PUT,    "/api/zones/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.DELETE, "/api/zones/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                // ── Rayons / Étagères / Emplacements : lecture tous, CRUD ADMIN+GESTIONNAIRE ──
                .requestMatchers(HttpMethod.GET,    "/api/rayons/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.GET,    "/api/etageres/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.GET,    "/api/emplacements/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,   "/api/rayons","/api/etageres","/api/emplacements").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.PUT,    "/api/rayons/**","/api/etageres/**","/api/emplacements/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.DELETE, "/api/rayons/**","/api/etageres/**","/api/emplacements/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                // ── Produits : lecture tous, CRUD ADMIN+GESTIONNAIRE ────────
                .requestMatchers(HttpMethod.GET,    "/api/produits/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,   "/api/produits").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.PUT,    "/api/produits/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.DELETE, "/api/produits/**").hasRole("ADMIN")

                // ── Stocks & mouvements : lecture tous ──────────────────────
                .requestMatchers(HttpMethod.GET, "/api/stocks/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.PATCH, "/api/stocks/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.GET, "/api/mouvements-stock/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")

                // ── Dashboard & alertes : tous les connectés ─────────────────
                .requestMatchers(HttpMethod.GET, "/api/dashboard/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/alertes/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/alertes/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                // ── Audit : ADMIN + AUDITEUR ─────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/audit/**").hasAnyRole("ADMIN","AUDITEUR")

                // ── Reporting : ADMIN + GESTIONNAIRE + AUDITEUR ─────────────
                .requestMatchers(HttpMethod.GET, "/api/reporting/**").hasAnyRole("ADMIN","GESTIONNAIRE","AUDITEUR")

                // ── Bons réception / sortie : ADMIN + GESTIONNAIRE + MAGASINIER ─
                .requestMatchers(HttpMethod.GET,    "/api/bon-receptions/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,   "/api/bon-receptions/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER")
                .requestMatchers(HttpMethod.PUT,    "/api/bon-receptions/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER")
                .requestMatchers(HttpMethod.DELETE, "/api/bon-receptions/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                .requestMatchers(HttpMethod.GET,    "/api/bon-sorties/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,   "/api/bon-sorties/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER")
                .requestMatchers(HttpMethod.PUT,    "/api/bon-sorties/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER")
                .requestMatchers(HttpMethod.DELETE, "/api/bon-sorties/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                // ── Transferts : ADMIN + GESTIONNAIRE + MAGASINIER ───────────
                .requestMatchers(HttpMethod.GET,   "/api/transferts/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,  "/api/transferts/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER")
                .requestMatchers(HttpMethod.PATCH, "/api/transferts/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER")
                // QR Code : accessible à tout utilisateur authentifié (tous rôles)
                .requestMatchers(HttpMethod.GET, "/api/qr/**").authenticated()

                // ── Inventaires : ADMIN + GESTIONNAIRE ────────────────────────
                .requestMatchers(HttpMethod.GET,   "/api/inventaires/**").hasAnyRole("ADMIN","GESTIONNAIRE","AUDITEUR")
                .requestMatchers(HttpMethod.POST,  "/api/inventaires/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.PATCH, "/api/inventaires/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                // ── Commandes fournisseurs : ADMIN + GESTIONNAIRE ─────────────
                .requestMatchers("/api/commandes-fournisseurs/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                // ── Catégories, fournisseurs : ADMIN + GESTIONNAIRE ───────────
                .requestMatchers(HttpMethod.GET,    "/api/categories/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,   "/api/categories/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.PUT,    "/api/categories/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                .requestMatchers(HttpMethod.GET,    "/api/fournisseurs/**").hasAnyRole("ADMIN","GESTIONNAIRE","MAGASINIER","AUDITEUR")
                .requestMatchers(HttpMethod.POST,   "/api/fournisseurs/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.PUT,    "/api/fournisseurs/**").hasAnyRole("ADMIN","GESTIONNAIRE")
                .requestMatchers(HttpMethod.DELETE, "/api/fournisseurs/**").hasAnyRole("ADMIN","GESTIONNAIRE")

                // --- Fallback générique : doit être en dernier avant anyRequest ---
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR")
                .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "GESTIONNAIRE")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "GESTIONNAIRE")
                .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("ADMIN", "GESTIONNAIRE")

                // Tout le reste : authentifié
                .anyRequest().authenticated()
            )
            .authenticationProvider(authProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
