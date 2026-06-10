package group5.ebay2.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class UserProfileDto {

    public record Request(

            @NotNull(message = "Auth user ID is required")
            UUID authUserId,

            @NotBlank(message = "Username is required")
            @Size(max = 50, message = "Username must not exceed 50 characters")
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            @Size(max = 150, message = "Email must not exceed 150 characters")
            String email,

            @NotBlank(message = "First name is required")
            @Size(max = 100, message = "First name must not exceed 100 characters")
            String firstName,

            @NotBlank(message = "Last name is required")
            @Size(max = 100, message = "Last name must not exceed 100 characters")
            String lastName,

            @NotBlank(message = "Phone number is required")
            @Size(max = 30, message = "Phone number must not exceed 30 characters")
            String phoneNumber,

            @Size(max = 500, message = "Profile image object key must not exceed 500 characters")
            String profileImageObjectKey

    ) {
    }

    public record UpdateRequest(

            @Size(max = 50, message = "Username must not exceed 50 characters")
            String username,

            @Email(message = "Email must be valid")
            @Size(max = 150, message = "Email must not exceed 150 characters")
            String email,

            @Size(max = 100, message = "First name must not exceed 100 characters")
            String firstName,

            @Size(max = 100, message = "Last name must not exceed 100 characters")
            String lastName,

            @Size(max = 30, message = "Phone number must not exceed 30 characters")
            String phoneNumber,

            @Size(max = 500, message = "Profile image object key must not exceed 500 characters")
            String profileImageObjectKey

    ) {
    }

    public record Response(
            UUID id,
            UUID authUserId,
            String username,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String profileImageObjectKey
    ) {
    }
}