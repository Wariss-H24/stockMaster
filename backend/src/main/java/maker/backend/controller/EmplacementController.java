package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.EmplacementDTO;
import maker.backend.service.EmplacementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emplacements")
@CrossOrigin(origins = "http://localhost:3000")
public class EmplacementController {

    private final EmplacementService service;

    public EmplacementController(EmplacementService service) { this.service = service; }

    @GetMapping
    public List<EmplacementDTO> tous() { return service.findAll(); }

    @GetMapping("/zone/{zoneId}")
    public List<EmplacementDTO> parZone(@PathVariable Long zoneId) { return service.findByZone(zoneId); }

    @GetMapping("/entrepot/{entrepotId}")
    public List<EmplacementDTO> parEntrepot(@PathVariable Long entrepotId) { return service.findByEntrepot(entrepotId); }

    @GetMapping("/disponibles")
    public List<EmplacementDTO> disponibles() { return service.findDisponibles(); }

    @GetMapping("/{id}")
    public EmplacementDTO obtenir(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<EmplacementDTO> creer(@Valid @RequestBody EmplacementDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping("/{id}")
    public EmplacementDTO modifier(@PathVariable Long id, @Valid @RequestBody EmplacementDTO dto) {
        return service.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
