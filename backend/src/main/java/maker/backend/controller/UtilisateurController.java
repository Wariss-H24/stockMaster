package maker.backend.controller;

import maker.backend.entity.Utilisateur;
import maker.backend.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) { this.service = service; }

    @GetMapping
    public List<Utilisateur> tous() { return service.findAll(); }

    @PostMapping
    public Utilisateur creer(@RequestBody Utilisateur u) { return service.save(u); }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> obtenir(@PathVariable Long id) { return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }

    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> mettreAJour(@PathVariable Long id, @RequestBody Utilisateur u) {
        return service.findById(id).map(existing -> {
            existing.setUsername(u.getUsername());
            existing.setNomComplet(u.getNomComplet());
            existing.setMotDePasse(u.getMotDePasse());
            existing.setActif(u.isActif());
            return ResponseEntity.ok(service.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        return service.findById(id).map(u -> { service.delete(id); return ResponseEntity.noContent().<Void>build(); }).orElse(ResponseEntity.notFound().build());
    }

}
