package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.TransfertDTO;
import maker.backend.service.TransfertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transferts")
@CrossOrigin(origins = "http://localhost:3000")
public class TransfertController {

    private final TransfertService service;

    public TransfertController(TransfertService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> creer(@Valid @RequestBody TransfertDTO dto) {
        service.transferer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
