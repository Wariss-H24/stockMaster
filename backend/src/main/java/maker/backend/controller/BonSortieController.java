package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.BonSortieDTO;
import maker.backend.service.BonSortieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bon-sorties")
@CrossOrigin(origins = "http://localhost:3000")
public class BonSortieController {

    private final BonSortieService service;

    public BonSortieController(BonSortieService service) {
        this.service = service;
    }

    @GetMapping
    public List<BonSortieDTO> tous() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public BonSortieDTO obtenir(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<BonSortieDTO> creer(@Valid @RequestBody BonSortieDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping("/{id}")
    public BonSortieDTO modifier(@PathVariable Long id, @Valid @RequestBody BonSortieDTO dto) {
        return service.modifier(id, dto);
    }

    @PostMapping("/{id}/valider")
    public BonSortieDTO valider(@PathVariable Long id) {
        return service.valider(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
