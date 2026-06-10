package group5.ebay2.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddUserDto {

    public record Request(
            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password
    ) {}

    public record Response(
            Long id,
            String username,
            String email
    ) {}
}
