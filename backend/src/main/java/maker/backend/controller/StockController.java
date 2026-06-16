package maker.backend.controller;

import maker.backend.dto.StockDTO;
import maker.backend.service.StockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "http://localhost:3000")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @GetMapping
    public List<StockDTO> tous() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public StockDTO obtenir(@PathVariable Long id) {
        return service.findById(id);
    }
}
