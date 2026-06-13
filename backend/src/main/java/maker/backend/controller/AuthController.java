package maker.backend.controller;

import jakarta.validation.Valid;
import maker.backend.dto.AuthResponse;
import maker.backend.dto.LoginRequest;
import maker.backend.dto.RegisterRequest;
import maker.backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        log.info("[LOGIN] username='{}' motDePasse longueur={}",
            req.getUsername(),
            req.getMotDePasse() != null ? req.getMotDePasse().length() : "NULL");
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        log.info("[REGISTER] username='{}'", req.getUsername());
        return ResponseEntity.ok(authService.register(req));
    }
}
