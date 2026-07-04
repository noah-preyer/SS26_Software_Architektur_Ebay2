package group5.ebay2.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserProfileDto {

    public record Request(

            @NotBlank(message = "Username is required")
            @Size(max = 50, message = "Username must not exceed 50 characters")
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            @Size(max = 150, message = "Email must not exceed 150 characters")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            @Size(max = 100, message = "First name must not exceed 100 characters")
            String firstName,

            @Size(max = 100, message = "Last name must not exceed 100 characters")
            String lastName,

            @Size(max = 30, message = "Phone number must not exceed 30 characters")
            String phoneNumber,

            @Size(max = 500, message = "Profile image object key must not exceed 500 characters")
            String profileImageObjectKey,

            @Size(max = 150)
            String addressStreet,

            @Size(max = 20)
            String addressHouseNumber,

            @Size(max = 20)
            String addressPostalCode,

            @Size(max = 100)
            String addressCity,

            @Size(max = 100)
            String addressCountry

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
            Long id,
            Long authUserId,
            String username,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String profileImageObjectKey
    ) {
    }
}