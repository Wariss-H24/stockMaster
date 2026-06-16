package maker.backend.controller;

import maker.backend.dto.MouvementStockDTO;
import maker.backend.service.MouvementStockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mouvements-stock")
@CrossOrigin(origins = "http://localhost:3000")
public class MouvementStockController {

    private final MouvementStockService service;

    public MouvementStockController(MouvementStockService service) {
        this.service = service;
    }

    @GetMapping
    public List<MouvementStockDTO> tous() {
        return service.findAll();
    }

    @GetMapping("/stock/{stockId}")
    public List<MouvementStockDTO> parStock(@PathVariable Long stockId) {
        return service.findByStockId(stockId);
    }

    @GetMapping("/type/{type}")
    public List<MouvementStockDTO> parType(@PathVariable String type) {
        return service.findByType(type);
    }
}
