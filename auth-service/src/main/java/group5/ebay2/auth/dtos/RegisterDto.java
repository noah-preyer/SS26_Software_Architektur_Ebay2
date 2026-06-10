package group5.ebay2.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDto {

    public record Request(
            @NotBlank @Email
            String email,

            @NotBlank @Size(min = 8)
            String password
    ) {}

    public record Response(
            Long id,
            String email
    ) {}
}
