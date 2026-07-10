package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.EtagereDTO;
import maker.backend.dto.EmplacementDTO;
import maker.backend.dto.RayonDTO;
import maker.backend.service.EmplacementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class EmplacementController {

    private final EmplacementService service;

    public EmplacementController(EmplacementService service) {
        this.service = service;
    }

    // ── RAYONS ────────────────────────────────────────────────────────────────

    @GetMapping("/rayons")
    public List<RayonDTO> tousRayons(@RequestParam(required = false) Long zoneId) {
        return zoneId != null ? service.findRayonsByZone(zoneId) : service.findAllRayons();
    }

    @GetMapping("/rayons/{id}")
    public RayonDTO obtenirRayon(@PathVariable Long id) {
        return service.findRayonById(id);
    }

    @PostMapping("/rayons")
    public ResponseEntity<RayonDTO> creerRayon(@Valid @RequestBody RayonDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creerRayon(dto));
    }

    @PutMapping("/rayons/{id}")
    public RayonDTO modifierRayon(@PathVariable Long id, @Valid @RequestBody RayonDTO dto) {
        return service.modifierRayon(id, dto);
    }

    @DeleteMapping("/rayons/{id}")
    public ResponseEntity<Void> desactiverRayon(@PathVariable Long id) {
        service.desactiverRayon(id);
        return ResponseEntity.noContent().build();
    }

    // ── ÉTAGÈRES ──────────────────────────────────────────────────────────────

    @GetMapping("/etageres")
    public List<EtagereDTO> toutesEtageres(@RequestParam(required = false) Long rayonId) {
        return rayonId != null ? service.findEtageresByRayon(rayonId) : service.findAllEtageres();
    }

    @GetMapping("/etageres/{id}")
    public EtagereDTO obtenirEtagere(@PathVariable Long id) {
        return service.findEtagereById(id);
    }

    @PostMapping("/etageres")
    public ResponseEntity<EtagereDTO> creerEtagere(@Valid @RequestBody EtagereDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creerEtagere(dto));
    }

    @PutMapping("/etageres/{id}")
    public EtagereDTO modifierEtagere(@PathVariable Long id, @Valid @RequestBody EtagereDTO dto) {
        return service.modifierEtagere(id, dto);
    }

    @DeleteMapping("/etageres/{id}")
    public ResponseEntity<Void> desactiverEtagere(@PathVariable Long id) {
        service.desactiverEtagere(id);
        return ResponseEntity.noContent().build();
    }

    // ── EMPLACEMENTS ──────────────────────────────────────────────────────────

    @GetMapping("/emplacements")
    public List<EmplacementDTO> tousEmplacements(
            @RequestParam(required = false) Long etagereId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long entrepotId) {
        if (etagereId  != null) return service.findEmplacementsByEtagere(etagereId);
        if (zoneId     != null) return service.findEmplacementsByZone(zoneId);
        if (entrepotId != null) return service.findEmplacementsByEntrepot(entrepotId);
        return service.findAllEmplacements();
    }

    @GetMapping("/emplacements/{id}")
    public EmplacementDTO obtenirEmplacement(@PathVariable Long id) {
        return service.findEmplacementById(id);
    }

    @PostMapping("/emplacements")
    public ResponseEntity<EmplacementDTO> creerEmplacement(@Valid @RequestBody EmplacementDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creerEmplacement(dto));
    }

    @PutMapping("/emplacements/{id}")
    public EmplacementDTO modifierEmplacement(@PathVariable Long id,
                                               @Valid @RequestBody EmplacementDTO dto) {
        return service.modifierEmplacement(id, dto);
    }

    @DeleteMapping("/emplacements/{id}")
    public ResponseEntity<Void> desactiverEmplacement(@PathVariable Long id) {
        service.desactiverEmplacement(id);
        return ResponseEntity.noContent().build();
    }
}
