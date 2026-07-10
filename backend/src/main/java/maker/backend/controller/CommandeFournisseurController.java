package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.config.Tracable;
import maker.backend.dto.CommandeFournisseurDTO;
import maker.backend.service.CommandeFournisseurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes-fournisseurs")
@CrossOrigin(origins = "http://localhost:3000")
public class CommandeFournisseurController {

    private final CommandeFournisseurService service;

    public CommandeFournisseurController(CommandeFournisseurService service) { this.service = service; }

    @GetMapping
    public List<CommandeFournisseurDTO> tous() { return service.findAll(); }

    @GetMapping("/{id}")
    public CommandeFournisseurDTO obtenir(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @Tracable(entite = "CommandeFournisseur", action = "CREER")
    public ResponseEntity<CommandeFournisseurDTO> creer(
            @Valid @RequestBody CommandeFournisseurDTO dto, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto, auth.getName()));
    }

    @PutMapping("/{id}")
    @Tracable(entite = "CommandeFournisseur", action = "MODIFIER")
    public CommandeFournisseurDTO modifier(@PathVariable Long id,
                                           @Valid @RequestBody CommandeFournisseurDTO dto) {
        return service.modifier(id, dto);
    }

    @PatchMapping("/{id}/envoyer")
    @Tracable(entite = "CommandeFournisseur", action = "ENVOYER")
    public CommandeFournisseurDTO envoyer(@PathVariable Long id) { return service.envoyer(id); }

    @PatchMapping("/{id}/receptionner")
    @Tracable(entite = "CommandeFournisseur", action = "RECEPTIONNER")
    public CommandeFournisseurDTO receptionner(@PathVariable Long id) { return service.receptionner(id); }

    @PatchMapping("/{id}/annuler")
    @Tracable(entite = "CommandeFournisseur", action = "ANNULER")
    public CommandeFournisseurDTO annuler(@PathVariable Long id) { return service.annuler(id); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
