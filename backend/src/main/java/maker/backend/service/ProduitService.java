package maker.backend.service;

import maker.backend.entity.Produit;
import maker.backend.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProduitService {
    private final ProduitRepository repo;

    public ProduitService(ProduitRepository repo) { this.repo = repo; }

    public List<Produit> findAllActifs() { return repo.findBySupprimeFalse(); }
    public Produit save(Produit p) { return repo.save(p); }
    public java.util.Optional<Produit> findById(Long id) { return repo.findById(id); }
    public void supprimerLogique(Long id) { repo.findById(id).ifPresent(p -> { p.setSupprime(true); repo.save(p); }); }
}
