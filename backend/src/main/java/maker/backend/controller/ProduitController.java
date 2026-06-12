package maker.backend.controller;

import maker.backend.entity.Produit;
import maker.backend.service.ProduitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService service;

    public ProduitController(ProduitService service) { this.service = service; }

    @GetMapping
    public List<Produit> tous() { return service.findAllActifs(); }

    @PostMapping
    public Produit creer(@RequestBody Produit p) { return service.save(p); }

    @GetMapping("/{id}")
    public ResponseEntity<Produit> obtenir(@PathVariable Long id) { return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }

    @PutMapping("/{id}")
    public ResponseEntity<Produit> modifier(@PathVariable Long id, @RequestBody Produit p) {
        return service.findById(id).map(existing -> {
            existing.setReference(p.getReference()); existing.setCodeBarre(p.getCodeBarre()); existing.setNom(p.getNom()); existing.setCategorie(p.getCategorie());
            existing.setDescription(p.getDescription()); existing.setPrixAchat(p.getPrixAchat()); existing.setPrixVente(p.getPrixVente()); existing.setPoids(p.getPoids()); existing.setVolume(p.getVolume());
            return ResponseEntity.ok(service.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) { service.supprimerLogique(id); return ResponseEntity.noContent().<Void>build(); }
}
