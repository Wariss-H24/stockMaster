package maker.backend.service;

import maker.backend.dto.UtilisateurDTO;
import maker.backend.entity.Utilisateur;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.UtilisateurMapper;
import maker.backend.repository.RoleRepository;
import maker.backend.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurService {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurService.class);

    private final UtilisateurRepository repo;
    private final RoleRepository roleRepo;
    private final UtilisateurMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository repo, RoleRepository roleRepo,
                              UtilisateurMapper mapper, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.roleRepo = roleRepo;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UtilisateurDTO> findAll() {
        return repo.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public UtilisateurDTO findById(Long id) {
        return repo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    public UtilisateurDTO creer(UtilisateurDTO dto) {
        log.info("[CREER] username='{}' motDePasse null={} longueur={}",
            dto.getUsername(),
            dto.getMotDePasse() == null,
            dto.getMotDePasse() != null ? dto.getMotDePasse().length() : 0);

        if (dto.getMotDePasse() == null || dto.getMotDePasse().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }
        if (repo.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }

        Utilisateur u = mapper.toEntity(dto);
        // Hashage BCrypt — le mot de passe brut ne doit jamais être stocké
        String hash = passwordEncoder.encode(dto.getMotDePasse());
        log.info("[CREER] hash généré : {}", hash.substring(0, 10) + "...");
        u.setMotDePasse(hash);
        u.setActif(true);

        if (dto.getRoles() != null) {
            for (String nomRole : dto.getRoles()) {
                roleRepo.findByNom(nomRole).ifPresent(r -> u.getRoles().add(r));
            }
        }
        return mapper.toDTO(repo.save(u));
    }

    public UtilisateurDTO modifier(Long id, UtilisateurDTO dto) {
        Utilisateur existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
        existing.setUsername(dto.getUsername());
        existing.setNomComplet(dto.getNomComplet());
        existing.setActif(dto.isActif());
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isBlank()) {
            log.info("[MODIFIER] Nouveau mot de passe pour '{}'", dto.getUsername());
            existing.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        }
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            existing.getRoles().clear();
            for (String nomRole : dto.getRoles()) {
                roleRepo.findByNom(nomRole).ifPresent(r -> existing.getRoles().add(r));
            }
        }
        return mapper.toDTO(repo.save(existing));
    }

    public void desactiver(Long id) {
        Utilisateur u = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
        u.setActif(false);
        repo.save(u);
    }
}
