package group5.ebay2.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {

    public record Request(

            // nimmt email oder username entgegen (siehe AuthService#authUser), das frontend
            // schickt hier ausschliesslich die email.
            @NotBlank(message = "Email or username is required")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record Response(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") long expiresIn,
            UserInfo user,
            String message
    ) {
        public record UserInfo(Long id, String email, String username) {
        }
    }
}