package group5.ebay2.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = System.getenv().getOrDefault("USER_SERVICE_URL", "http://user-service:8080");
    }

    public UserDto getUser(UUID userId) {
        try {
            return restTemplate.getForObject(userServiceUrl + "/user/" + userId, UserDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
