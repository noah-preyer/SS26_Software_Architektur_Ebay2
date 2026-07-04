
package group5.ebay2.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AddressDto {

    public record Request(

            @NotBlank(message = "Street is required")
            @Size(max = 150)
            String street,

            @NotBlank(message = "House number is required")
            @Size(max = 20)
            String houseNumber,

            @NotBlank(message = "Postal code is required")
            @Size(max = 20)
            String postalCode,

            @NotBlank(message = "City is required")
            @Size(max = 100)
            String city,

            @NotBlank(message = "Country is required")
            @Size(max = 100)
            String country,

            @NotBlank(message = "Address type is required")
            String addressTypeCode,

            @NotNull(message = "Default address flag is required")
            Boolean defaultAddress

    ) {
    }

    public record Response(
            Long id,
            String street,
            String houseNumber,
            String postalCode,
            String city,
            String country,
            String addressTypeCode,
            String addressTypeDisplayName,
            Boolean defaultAddress
    ) {
    }
}