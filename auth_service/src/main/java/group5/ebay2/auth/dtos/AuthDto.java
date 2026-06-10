package group5.ebay2.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public class AuthDto {

    public record Request(

            @NotBlank(message = "Email or username is required")
            String emailOrUsername,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record Response(
            String accessToken,
            String message
    ) {
    }
}