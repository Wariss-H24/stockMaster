package maker.backend.service;

import maker.backend.dto.FournisseurDTO;
import maker.backend.entity.Fournisseur;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.FournisseurMapper;
import maker.backend.repository.FournisseurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FournisseurService {

    private final FournisseurRepository repo;
    private final FournisseurMapper mapper;

    public FournisseurService(FournisseurRepository repo, FournisseurMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<FournisseurDTO> findAll() {
        return repo.findByActifTrue().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public FournisseurDTO findById(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
    }

    public FournisseurDTO creer(FournisseurDTO dto) {
        Fournisseur f = mapper.toEntity(dto);
        return mapper.toDTO(repo.save(f));
    }

    public FournisseurDTO modifier(Long id, FournisseurDTO dto) {
        Fournisseur existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
        existing.setNom(dto.getNom());
        existing.setAdresse(dto.getAdresse());
        existing.setTelephone(dto.getTelephone());
        existing.setEmail(dto.getEmail());
        existing.setContactPrincipal(dto.getContactPrincipal());
        existing.setActif(dto.isActif());
        return mapper.toDTO(repo.save(existing));
    }

    public void desactiver(Long id) {
        Fournisseur f = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
        f.setActif(false);
        repo.save(f);
    }
}
