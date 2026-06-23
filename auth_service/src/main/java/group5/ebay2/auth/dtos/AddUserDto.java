package group5.ebay2.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class AddUserDto {

    public record Request(

            // optional: frontend schickt keinen username, AuthService leitet ihn dann aus der email ab.
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password
    ) {
    }

    public record Response(
            Long id,
            String username,
            String email
    ) {
    }
}