package maker.backend.controller;

import maker.backend.dto.AuditLogDTO;
import maker.backend.service.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "http://localhost:3000")
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> tous(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Map.of(
                "logs", service.findAll(page, size),
                "total", service.countAll(),
                "page", page,
                "size", size
        );
    }

    @GetMapping("/recherche")
    public List<AuditLogDTO> recherche(
            @RequestParam(required = false) String entite,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String utilisateur,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return service.rechercherAvance(entite, action, utilisateur, page, size);
    }

    @GetMapping("/entite/{entite}/{id}")
    public List<AuditLogDTO> parEntite(@PathVariable String entite,
                                        @PathVariable Long id) {
        return service.findByEntite(entite, id);
    }
}
