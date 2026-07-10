package maker.backend.controller;

import maker.backend.dto.DashboardDTO;
import maker.backend.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/kpis")
    public DashboardDTO kpis() {
        return service.getKpis();
    }
}
