package group5.ebay2.auth;

import group5.ebay2.auth.dtos.LoginDto;
import group5.ebay2.auth.dtos.RegisterDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterDto.Response> register(@Valid @RequestBody RegisterDto.Request request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginDto.Response> login(@Valid @RequestBody LoginDto.Request request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
