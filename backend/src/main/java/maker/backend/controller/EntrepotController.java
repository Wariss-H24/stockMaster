package maker.backend.controller;

import maker.backend.entity.Entrepot;
import maker.backend.service.EntrepotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entrepots")
public class EntrepotController {

    private final EntrepotService service;

    public EntrepotController(EntrepotService service) { this.service = service; }

    @GetMapping
    public List<Entrepot> tous() { return service.findAll(); }

    @PostMapping
    public Entrepot creer(@RequestBody Entrepot e) { return service.save(e); }

    @GetMapping("/{id}")
    public ResponseEntity<Entrepot> obtenir(@PathVariable Long id) { return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }

    @PutMapping("/{id}")
    public ResponseEntity<Entrepot> modifier(@PathVariable Long id, @RequestBody Entrepot e) {
        return service.findById(id).map(existing -> {
            existing.setNom(e.getNom()); existing.setAdresse(e.getAdresse()); existing.setResponsable(e.getResponsable());
            existing.setCapaciteTotale(e.getCapaciteTotale()); existing.setCapaciteUtilisee(e.getCapaciteUtilisee()); existing.setActif(e.isActif());
            return ResponseEntity.ok(service.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) { return service.findById(id).map(e -> { service.delete(id); return ResponseEntity.noContent().<Void>build(); }).orElse(ResponseEntity.notFound().build()); }
}
