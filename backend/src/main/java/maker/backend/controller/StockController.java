package maker.backend.controller;

import maker.backend.dto.StockDTO;
import maker.backend.service.StockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /** Met à jour stockMin et stockMax d'un stock existant. */
    @PatchMapping("/{id}/seuils")
    public StockDTO majSeuils(@PathVariable Long id,
                               @RequestBody Map<String, Integer> seuils) {
        return service.majSeuils(id,
            seuils.getOrDefault("stockMin", 0),
            seuils.getOrDefault("stockMax", 0));
    }
}
