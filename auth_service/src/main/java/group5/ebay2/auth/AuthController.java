package group5.ebay2.auth;

import group5.ebay2.error.ErrorResponse;
import group5.ebay2.auth.dtos.AddUserDto;
import group5.ebay2.auth.dtos.AuthDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> addUser(@Valid @RequestBody AddUserDto.Request request) {
        try {
            AddUserDto.Response response = authService.addUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AuthExceptions.UserAlreadyExistsException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authUser(@Valid @RequestBody AuthDto.Request request) {
        try {
            AuthDto.Response response = authService.authUser(request);
            return ResponseEntity.ok(response);
        } catch (AuthExceptions.InvalidPasswordException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(ex.getMessage()));
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            authService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (AuthExceptions.UserNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(ex.getMessage()));
        }
    }
}