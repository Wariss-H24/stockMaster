package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.BonReceptionDTO;
import maker.backend.dto.FournisseurDTO;
import maker.backend.service.BonReceptionService;
import maker.backend.service.FournisseurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@CrossOrigin(origins = "http://localhost:3000")
public class FournisseurController {

    private final FournisseurService service;
    private final BonReceptionService bonReceptionService;

    public FournisseurController(FournisseurService service, BonReceptionService bonReceptionService) {
        this.service = service;
        this.bonReceptionService = bonReceptionService;
    }

    @GetMapping
    public List<FournisseurDTO> tous() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public FournisseurDTO obtenir(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/livraisons")
    public List<BonReceptionDTO> livraisons(@PathVariable Long id) {
        return bonReceptionService.findByFournisseurId(id);
    }

    @PostMapping
    public ResponseEntity<FournisseurDTO> creer(@Valid @RequestBody FournisseurDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping("/{id}")
    public FournisseurDTO modifier(@PathVariable Long id, @Valid @RequestBody FournisseurDTO dto) {
        return service.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        service.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
