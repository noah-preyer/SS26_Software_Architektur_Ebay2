package group5.ebay2.auth;

import group5.ebay2.auth.dtos.LoginDto;
import group5.ebay2.auth.dtos.RegisterDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterDto.Response register(RegisterDto.Request request) {
        if (authRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        AuthUser user = new AuthUser(request.email(), passwordEncoder.encode(request.password()));
        authRepository.save(user);
        return new RegisterDto.Response(user.getId(), user.getEmail());
    }

    public LoginDto.Response login(LoginDto.Request request) {
        AuthUser user = authRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new LoginDto.Response(token);
    }
}
