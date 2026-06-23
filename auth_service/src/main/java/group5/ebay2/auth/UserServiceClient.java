package group5.ebay2.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${services.user.url}") String userServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(userServiceUrl).build();
    }

    public void createProfile(Long authUserId, String username, String email) {
        restClient.post()
                .uri("/user")
                .body(new CreateProfileRequest(authUserId, username, email, null, null, null, null))
                .retrieve()
                .toBodilessEntity();
    }

    record CreateProfileRequest(
            Long authUserId,
            String username,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String profileImageObjectKey
    ) {}
}
