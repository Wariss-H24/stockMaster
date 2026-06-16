package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.BonReceptionDTO;
import maker.backend.service.BonReceptionService;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<BonReceptionDTO> creer(@Valid @RequestBody BonReceptionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping("/{id}")
    public BonReceptionDTO modifier(@PathVariable Long id, @Valid @RequestBody BonReceptionDTO dto) {
        return service.modifier(id, dto);
    }

    @PostMapping("/{id}/valider")
    public BonReceptionDTO valider(@PathVariable Long id) {
        return service.valider(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
