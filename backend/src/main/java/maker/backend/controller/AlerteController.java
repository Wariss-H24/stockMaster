package maker.backend.controller;

import maker.backend.dto.AlerteDTO;
import maker.backend.service.AlerteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@CrossOrigin(origins = "http://localhost:3000")
public class AlerteController {

    private final AlerteService service;

    public AlerteController(AlerteService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlerteDTO> toutes() {
        return service.getAlertes();
    }

    @GetMapping("/critiques")
    public List<AlerteDTO> critiques() {
        return service.getAlertesCritiques();
    }

    @PostMapping("/envoyer-email")
    public void envoyerEmail() {
        service.envoyerEmailAlertes(service.getAlertesCritiques());
    }
}
