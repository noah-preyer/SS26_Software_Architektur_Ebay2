package group5.ebay2.auth;

import group5.ebay2.error.ErrorResponse;
import group5.ebay2.auth.dtos.AddUserDto;
import group5.ebay2.auth.dtos.AuthDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> addUser(@Valid @RequestBody AddUserDto.Request request) {
        log.info("POST /register — username={}, email={}", request.username(), request.email());
        try {
            AddUserDto.Response response = authService.addUser(request);
            log.info("POST /register — success userId={}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AuthExceptions.UserAlreadyExistsException ex) {
            log.warn("POST /register — conflict: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(ex.getMessage()));
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> authUser(@Valid @RequestBody AuthDto.Request request) {
        log.info("POST /login — emailOrUsername={}", request.emailOrUsername());
        try {
            AuthDto.Response response = authService.authUser(request);
            log.info("POST /login — success");
            return ResponseEntity.ok(response);
        } catch (AuthExceptions.InvalidPasswordException ex) {
            log.warn("POST /login — failed: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(ex.getMessage()));
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        log.info("DELETE /user/{}", id);
        try {
            authService.deleteUser(id);
            log.info("DELETE /user/{} — success", id);
            return ResponseEntity.noContent().build();
        } catch (AuthExceptions.UserNotFoundException ex) {
            log.warn("DELETE /user/{} — not found", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(ex.getMessage()));
        }
    }
}