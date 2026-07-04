package group5.ebay2.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(@Value("${services.user.url}") String userUrl) {
        this.restClient = RestClient.builder().baseUrl(userUrl).build();
    }

    public String getUsernameById(Long userId) {
        try {
            UserResponse response = restClient.get()
                    .uri("/{id}", userId)
                    .retrieve()
                    .body(UserResponse.class);
            return response != null ? response.username() : null;
        } catch (RestClientException e) {
            return null;
        }
    }

    record UserResponse(Long id, String username, String email) {}
}