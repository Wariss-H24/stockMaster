package maker.backend.service;

import maker.backend.entity.Utilisateur;
import maker.backend.repository.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Implémentation de UserDetailsService pour Spring Security.
 * Charge l'utilisateur depuis la base et convertit ses rôles en authorities Spring.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository repo;

    public UserDetailsServiceImpl(UtilisateurRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));

        // Convertit les rôles en authorities Spring Security (préfixe ROLE_ attendu par Spring)
        var authorities = u.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNom()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getMotDePasse(),
                u.isActif(),   // enabled
                true,          // accountNonExpired
                true,          // credentialsNonExpired
                true,          // accountNonLocked
                authorities
        );
    }
}
