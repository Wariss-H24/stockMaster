package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.config.Tracable;
import maker.backend.dto.TransfertDTO;
import maker.backend.service.TransfertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transferts")
@CrossOrigin(origins = "http://localhost:3000")
public class TransfertController {

    private final TransfertService service;

    public TransfertController(TransfertService service) {
        this.service = service;
    }

    @GetMapping
    public List<TransfertDTO> tous() { return service.findAll(); }

    @GetMapping("/{id}")
    public TransfertDTO obtenir(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @Tracable(entite = "Transfert", action = "CREER")
    public ResponseEntity<TransfertDTO> creer(@Valid @RequestBody TransfertDTO dto, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto, auth.getName()));
    }

    @PatchMapping("/{id}/expedier")
    @Tracable(entite = "Transfert", action = "EXPEDIER")
    public TransfertDTO expedier(@PathVariable Long id) { return service.expedier(id); }

    @PatchMapping("/{id}/recevoir")
    @Tracable(entite = "Transfert", action = "RECEVOIR")
    public TransfertDTO recevoir(@PathVariable Long id) { return service.recevoir(id); }

    @PatchMapping("/{id}/annuler")
    @Tracable(entite = "Transfert", action = "ANNULER")
    public TransfertDTO annuler(@PathVariable Long id) { return service.annuler(id); }
}
