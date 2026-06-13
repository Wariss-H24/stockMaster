package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.EntrepotDTO;
import maker.backend.service.EntrepotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entrepots")
@CrossOrigin(origins = "http://localhost:3000")
public class EntrepotController {

    private final EntrepotService service;

    public EntrepotController(EntrepotService service) {
        this.service = service;
    }

    @GetMapping
    public List<EntrepotDTO> tous() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public EntrepotDTO obtenir(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<EntrepotDTO> creer(@Valid @RequestBody EntrepotDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping("/{id}")
    public EntrepotDTO modifier(@PathVariable Long id, @Valid @RequestBody EntrepotDTO dto) {
        return service.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        service.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
