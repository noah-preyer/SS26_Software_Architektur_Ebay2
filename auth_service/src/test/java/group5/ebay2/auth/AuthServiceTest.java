package group5.ebay2.auth;

import group5.ebay2.auth.dtos.AddUserDto;
import group5.ebay2.auth.dtos.AuthDto;
import group5.ebay2.auth.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();
    }

    @Test
    void addUser_shouldCreateUser() {
        AddUserDto.Request request = new AddUserDto.Request(
                "newuser", "new@example.com", "password123"
        );

        AddUserDto.Response response = authService.addUser(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.email()).isEqualTo("new@example.com");
    }

    @Test
    void addUser_shouldThrowOnDuplicateEmail() {
        authService.addUser(new AddUserDto.Request("user1", "dup@example.com", "password123"));

        AddUserDto.Request duplicate = new AddUserDto.Request(
                "user2", "dup@example.com", "otherpass123"
        );

        assertThatThrownBy(() -> authService.addUser(duplicate))
                .isInstanceOf(AuthExceptions.UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void addUser_shouldThrowOnDuplicateUsername() {
        authService.addUser(new AddUserDto.Request("dupuser", "a@example.com", "password123"));

        AddUserDto.Request duplicate = new AddUserDto.Request(
                "dupuser", "b@example.com", "otherpass123"
        );

        assertThatThrownBy(() -> authService.addUser(duplicate))
                .isInstanceOf(AuthExceptions.UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void authUser_shouldAuthenticateWithEmail() {
        authService.addUser(new AddUserDto.Request("testuser", "test@example.com", "securePass1"));

        AuthDto.Request authRequest = new AuthDto.Request("test@example.com", "securePass1");
        AuthDto.Response response = authService.authUser(authRequest);

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.message()).isEqualTo("Authentication successful");
    }

    @Test
    void authUser_shouldAuthenticateWithUsername() {
        authService.addUser(new AddUserDto.Request("testuser", "test@example.com", "securePass1"));

        AuthDto.Request authRequest = new AuthDto.Request("testuser", "securePass1");
        AuthDto.Response response = authService.authUser(authRequest);

        assertThat(response.accessToken()).isNotBlank();
    }

    @Test
    void authUser_shouldThrowOnWrongPassword() {
        authService.addUser(new AddUserDto.Request("testuser", "test@example.com", "correctPass1"));

        AuthDto.Request authRequest = new AuthDto.Request("test@example.com", "wrongPass1");

        assertThatThrownBy(() -> authService.authUser(authRequest))
                .isInstanceOf(AuthExceptions.InvalidPasswordException.class)
                .hasMessageContaining("Invalid email/username or password");
    }

    @Test
    void authUser_shouldThrowOnUnknownUser() {
        AuthDto.Request authRequest = new AuthDto.Request("unknown@example.com", "somePass1");

        assertThatThrownBy(() -> authService.authUser(authRequest))
                .isInstanceOf(AuthExceptions.InvalidPasswordException.class)
                .hasMessageContaining("Invalid email/username or password");
    }

    @Test
    void deleteUser_shouldDeleteExistingUser() {
        AddUserDto.Response created = authService.addUser(
                new AddUserDto.Request("todelete", "delete@example.com", "password123")
        );

        authService.deleteUser(created.id());

        assertThat(userRepository.findById(created.id())).isEmpty();
    }

    @Test
    void deleteUser_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> authService.deleteUser(UUID.randomUUID()))
                .isInstanceOf(AuthExceptions.UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
