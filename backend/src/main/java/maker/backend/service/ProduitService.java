package maker.backend.service;

import maker.backend.dto.ProduitDTO;
import maker.backend.entity.Produit;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.ProduitMapper;
import maker.backend.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository repo;
    private final ProduitMapper mapper;

    public ProduitService(ProduitRepository repo, ProduitMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    // Retourne uniquement les produits non supprimés
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
        return mapper.toDTO(repo.save(p));
    }

    public ProduitDTO modifier(Long id, ProduitDTO dto) {
        Produit existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
        existing.setReference(dto.getReference());
        existing.setCodeBarre(dto.getCodeBarre());
        existing.setNom(dto.getNom());
        existing.setCategorie(dto.getCategorie());
        existing.setDescription(dto.getDescription());
        existing.setPrixAchat(dto.getPrixAchat());
        existing.setPrixVente(dto.getPrixVente());
        existing.setPoids(dto.getPoids());
        existing.setVolume(dto.getVolume());
        return mapper.toDTO(repo.save(existing));
    }

    // Suppression logique : le produit reste en base avec supprime = true
    public void supprimerLogique(Long id) {
        Produit p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
        p.setSupprime(true);
        repo.save(p);
    }
}
