package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.ProduitDTO;
import maker.backend.service.ProduitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin(origins = "http://localhost:3000")
public class ProduitController {

    private final ProduitService service;

    public ProduitController(ProduitService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProduitDTO> tous() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProduitDTO obtenir(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProduitDTO> creer(@Valid @RequestBody ProduitDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping("/{id}")
    public ProduitDTO modifier(@PathVariable Long id, @Valid @RequestBody ProduitDTO dto) {
        return service.modifier(id, dto);
    }

    // Suppression logique (le produit reste en base)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimerLogique(id);
        return ResponseEntity.noContent().build();
    }
}
