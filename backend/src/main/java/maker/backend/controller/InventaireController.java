package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.config.Tracable;
import maker.backend.dto.InventaireDTO;
import maker.backend.dto.InventaireLigneDTO;
import maker.backend.service.InventaireService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventaires")
@CrossOrigin(origins = "http://localhost:3000")
public class InventaireController {

    private final InventaireService service;

    public InventaireController(InventaireService service) { this.service = service; }

    @GetMapping
    public List<InventaireDTO> tous() { return service.findAll(); }

    @GetMapping("/{id}")
    public InventaireDTO obtenir(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @Tracable(entite = "Inventaire", action = "CREER")
    public ResponseEntity<InventaireDTO> creer(@Valid @RequestBody InventaireDTO dto, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto, auth.getName()));
    }

    @PatchMapping("/{id}/lignes")
    public InventaireDTO mettreAJourLignes(@PathVariable Long id,
                                            @RequestBody List<InventaireLigneDTO> lignes) {
        return service.mettreAJourLignes(id, lignes);
    }

    @PatchMapping("/{id}/cloturer")
    @Tracable(entite = "Inventaire", action = "CLOTURER")
    public InventaireDTO cloturer(@PathVariable Long id) { return service.cloturer(id); }

    @PatchMapping("/{id}/annuler")
    @Tracable(entite = "Inventaire", action = "ANNULER")
    public InventaireDTO annuler(@PathVariable Long id) { return service.annuler(id); }
}
