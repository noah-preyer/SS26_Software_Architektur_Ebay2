package group5.ebay2.auth;

import org.slf4j.Logger;
import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import group5.ebay2.auth.dtos.AddUserDto;
import group5.ebay2.auth.dtos.AuthDto;
import group5.ebay2.auth.repositories.UserRepository;
import group5.ebay2.auth.AuthExceptions;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClientException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;

    public AuthService(UserRepository userRepository, JwtService jwtService, UserServiceClient userServiceClient) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userServiceClient = userServiceClient;
    }

    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public void checkClass() {
        log.info("Test");
    }

    public AddUserDto.Response addUser(AddUserDto.Request request) {
        log.info("Creating user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new AuthExceptions.UserAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new AuthExceptions.UserAlreadyExistsException("Username already exists");
        }
        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);

        //try {
        //    userServiceClient.createProfile(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
        //} catch (RestClientException e) {
        //    userRepository.delete(savedUser);
        //    throw new RuntimeException("Could not create user profile, registration rolled back: " + e.getMessage());
        //}

        return new AddUserDto.Response(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }
    public AuthDto.Response authUser(AuthDto.Request request) {
        User user = userRepository
                .findByEmailOrUsername(request.emailOrUsername(), request.emailOrUsername())
                .orElseThrow(() -> new AuthExceptions.InvalidPasswordException(
                        "Invalid email/username or password"
                ));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthExceptions.InvalidPasswordException(
                    "Invalid email/username or password"
            );
        }

        String accessToken = jwtService.generateAccessToken(user);

        return new AuthDto.Response(
                accessToken,
                "Authentication successful"
        );
    }
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new AuthExceptions.UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
    }
}