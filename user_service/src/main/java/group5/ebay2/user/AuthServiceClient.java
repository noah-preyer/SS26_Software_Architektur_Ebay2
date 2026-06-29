package group5.ebay2.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestClient restClient;
    private final String internalApiKey;

    public AuthServiceClient(
            @Value("${services.auth.url}") String authServiceUrl,
            @Value("${services.auth.internal-key}") String internalApiKey) {
        this.restClient = RestClient.builder().baseUrl(authServiceUrl).build();
        this.internalApiKey = internalApiKey;
    }

    private RestClient.Builder withInternalKey(RestClient.Builder builder) {
        return builder.defaultHeader("X-Internal-Key", internalApiKey);
    }

    public AuthUser createUser(String username, String email, String password) {
        return restClient.post()
                .uri("/register")
                .body(new CreateUserRequest(username, email, password))
                .retrieve()
                .body(AuthUser.class);
    }

    public void deleteUser(UUID authUserId) {
        try {
            restClient.delete()
                    .uri("/delete/{id}", authUserId)
                    .header("X-Internal-Key", internalApiKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to delete auth user {}: {}", authUserId, e.getMessage());
        }
    }

    public AuthUser getUser(UUID authUserId) {
        return restClient.get()
                .uri("/user/{id}", authUserId)
                .retrieve()
                .body(AuthUser.class);
    }

    record CreateUserRequest(
            String username,
            String email,
            String password
    ) {}

    public record AuthUser(
            UUID id,
            String username,
            String email
    ) {}
}
