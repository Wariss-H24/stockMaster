package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.config.Tracable;
import maker.backend.dto.BonReceptionDTO;
import maker.backend.service.BonReceptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bon-receptions")
@CrossOrigin(origins = "http://localhost:3000")
public class BonReceptionController {

    private final BonReceptionService service;

    public BonReceptionController(BonReceptionService service) {
        this.service = service;
    }

    @GetMapping
    public List<BonReceptionDTO> tous() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public BonReceptionDTO obtenir(@PathVariable Long id) {
        return service.findById(id);
    }

    // La création manuelle est désactivée.
    // Les bons de réception sont générés automatiquement lors de la réception d'une commande fournisseur.

    @PutMapping("/{id}")
    @Tracable(entite = "BonReception", action = "MODIFIER")
    public BonReceptionDTO modifier(@PathVariable Long id, @Valid @RequestBody BonReceptionDTO dto) {
        return service.modifier(id, dto);
    }

    @PostMapping("/{id}/valider")
    @Tracable(entite = "BonReception", action = "VALIDER")
    public BonReceptionDTO valider(@PathVariable Long id) {
        return service.valider(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
