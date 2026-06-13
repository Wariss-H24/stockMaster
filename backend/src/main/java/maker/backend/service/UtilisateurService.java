package maker.backend.service;

import maker.backend.dto.UtilisateurDTO;
import maker.backend.entity.Role;
import maker.backend.entity.Utilisateur;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.UtilisateurMapper;
import maker.backend.repository.RoleRepository;
import maker.backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository repo;
    private final RoleRepository roleRepo;
    private final UtilisateurMapper mapper;

    public UtilisateurService(UtilisateurRepository repo, RoleRepository roleRepo, UtilisateurMapper mapper) {
        this.repo = repo;
        this.roleRepo = roleRepo;
        this.mapper = mapper;
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
        Utilisateur u = mapper.toEntity(dto);
        // Résoudre les rôles depuis leur nom
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
            existing.setMotDePasse(dto.getMotDePasse());
        }
        return mapper.toDTO(repo.save(existing));
    }

    // Désactivation logique (pas de suppression physique)
    public void desactiver(Long id) {
        Utilisateur u = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
        u.setActif(false);
        repo.save(u);
    }
}
