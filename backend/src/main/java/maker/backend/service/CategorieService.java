package maker.backend.service;

import maker.backend.dto.CategorieDTO;
import maker.backend.entity.Categorie;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.CategorieMapper;
import maker.backend.repository.CategorieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategorieService {

    private final CategorieRepository repo;
    private final CategorieMapper mapper;

    public CategorieService(CategorieRepository repo, CategorieMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<CategorieDTO> findAll() {
        return repo.findByActifTrue().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public CategorieDTO findById(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }

    public CategorieDTO creer(CategorieDTO dto) {
        Categorie c = mapper.toEntity(dto);
        return mapper.toDTO(repo.save(c));
    }

    public CategorieDTO modifier(Long id, CategorieDTO dto) {
        Categorie existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
        existing.setNom(dto.getNom());
        existing.setDescription(dto.getDescription());
        existing.setActif(dto.isActif());
        return mapper.toDTO(repo.save(existing));
    }

    public void desactiver(Long id) {
        Categorie c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
        c.setActif(false);
        repo.save(c);
    }
}
