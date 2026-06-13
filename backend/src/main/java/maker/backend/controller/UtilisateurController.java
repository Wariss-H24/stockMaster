package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.UtilisateurDTO;
import maker.backend.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "http://localhost:3000")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public List<UtilisateurDTO> tous() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UtilisateurDTO obtenir(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<UtilisateurDTO> creer(@Valid @RequestBody UtilisateurDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping("/{id}")
    public UtilisateurDTO modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurDTO dto) {
        return service.modifier(id, dto);
    }

    // Désactivation logique
    @DeleteMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        service.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    // Suppression physique définitive — ADMIN seulement (règle dans SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
