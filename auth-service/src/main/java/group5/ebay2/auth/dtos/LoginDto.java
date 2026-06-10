package group5.ebay2.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginDto {

    public record Request(
            @NotBlank @Email
            String email,

            @NotBlank
            String password
    ) {}

    public record Response(
            String token
    ) {}
}
