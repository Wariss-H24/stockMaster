package maker.backend.service;

import maker.backend.dto.ProduitDTO;
import maker.backend.entity.Categorie;
import maker.backend.entity.Produit;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.ProduitMapper;
import maker.backend.repository.CategorieRepository;
import maker.backend.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository repo;
    private final CategorieRepository categorieRepo;
    private final ProduitMapper mapper;

    public ProduitService(ProduitRepository repo, CategorieRepository categorieRepo, ProduitMapper mapper) {
        this.repo = repo;
        this.categorieRepo = categorieRepo;
        this.mapper = mapper;
    }

    public List<ProduitDTO> findAll() {
        return repo.findBySupprimeFalse().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public ProduitDTO findById(Long id) {
        return repo.findById(id)
                .filter(p -> !p.isSupprime())
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
    }

    public ProduitDTO creer(ProduitDTO dto) {
        Produit p = mapper.toEntity(dto);
        p.setCategorie(resolveCategorie(dto.getCategorieId()));
        return mapper.toDTO(repo.save(p));
    }

    public ProduitDTO modifier(Long id, ProduitDTO dto) {
        Produit existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
        existing.setReference(dto.getReference());
        existing.setCodeBarre(dto.getCodeBarre());
        existing.setNom(dto.getNom());
        existing.setCategorie(resolveCategorie(dto.getCategorieId()));
        existing.setDescription(dto.getDescription());
        existing.setPrixAchat(dto.getPrixAchat());
        existing.setPrixVente(dto.getPrixVente());
        existing.setPoids(dto.getPoids());
        existing.setVolume(dto.getVolume());
        return mapper.toDTO(repo.save(existing));
    }

    public void supprimerLogique(Long id) {
        Produit p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
        p.setSupprime(true);
        repo.save(p);
    }

    private Categorie resolveCategorie(Long categorieId) {
        if (categorieId == null) return null;
        return categorieRepo.findById(categorieId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + categorieId));
    }
}
