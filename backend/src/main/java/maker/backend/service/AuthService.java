package maker.backend.service;

import maker.backend.config.JwtUtil;
import maker.backend.dto.AuthResponse;
import maker.backend.dto.LoginRequest;
import maker.backend.dto.RegisterRequest;
import maker.backend.entity.Role;
import maker.backend.entity.Utilisateur;
import maker.backend.repository.RoleRepository;
import maker.backend.repository.UtilisateurRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service d'authentification : login et inscription.
 */
@Service
public class AuthService {

    private final UtilisateurRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthService(UtilisateurRepository userRepo, RoleRepository roleRepo,
                       PasswordEncoder passwordEncoder, AuthenticationManager authManager,
                       JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Connexion : vérifie les credentials, génère un token JWT.
     */
    public AuthResponse login(LoginRequest req) {
        try {
            // Spring Security vérifie username + mot de passe hashé
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getMotDePasse())
            );
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Identifiants incorrects");
        }

        Utilisateur u = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!u.isActif()) {
            throw new IllegalArgumentException("Compte désactivé");
        }

        String token = jwtUtil.genererToken(u.getUsername());
        var roles = u.getRoles().stream().map(Role::getNom).collect(Collectors.toSet());

        return new AuthResponse(token, u.getUsername(), u.getNomComplet(), roles);
    }

    /**
     * Inscription : crée un compte avec le rôle MAGASINIER par défaut.
     */
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }

        Utilisateur u = new Utilisateur();
        u.setUsername(req.getUsername());
        // Hashage BCrypt du mot de passe
        u.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        u.setNomComplet(req.getNomComplet());
        u.setActif(true);

        // Rôle par défaut : MAGASINIER
        roleRepo.findByNom("MAGASINIER").ifPresent(r -> u.getRoles().add(r));
        userRepo.save(u);

        String token = jwtUtil.genererToken(u.getUsername());
        var roles = u.getRoles().stream().map(Role::getNom).collect(Collectors.toSet());

        return new AuthResponse(token, u.getUsername(), u.getNomComplet(), roles);
    }
}
