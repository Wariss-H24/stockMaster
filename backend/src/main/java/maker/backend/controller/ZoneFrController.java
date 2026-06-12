package maker.backend.controller;

import maker.backend.entity.ZoneFr;
import maker.backend.service.ZoneFrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneFrController {

    private final ZoneFrService service;

    public ZoneFrController(ZoneFrService service) { this.service = service; }

    @GetMapping
    public List<ZoneFr> tous() { return service.findAll(); }

    @PostMapping
    public ZoneFr creer(@RequestBody ZoneFr z) { return service.save(z); }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneFr> obtenir(@PathVariable Long id) { return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneFr> modifier(@PathVariable Long id, @RequestBody ZoneFr z) {
        return service.findById(id).map(existing -> {
            existing.setNom(z.getNom()); existing.setCapaciteTotale(z.getCapaciteTotale()); existing.setCapaciteUtilisee(z.getCapaciteUtilisee()); existing.setEntrepot(z.getEntrepot());
            return ResponseEntity.ok(service.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) { return service.findById(id).map(z -> { service.delete(id); return ResponseEntity.noContent().<Void>build(); }).orElse(ResponseEntity.notFound().build()); }
}
