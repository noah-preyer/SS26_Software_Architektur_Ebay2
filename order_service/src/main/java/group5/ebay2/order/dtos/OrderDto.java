package group5.ebay2.order.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderDto {

    public record CreateRequest(

            @NotNull(message = "User ID is required")
            UUID userId,

            @NotNull(message = "Product ID is required")
            UUID productId,

            @NotBlank(message = "Currency is required")
            @Size(min = 3, max = 3, message = "Currency must be 3 characters")
            String currency

    ) {
    }

    public record StatusUpdateRequest(

            @NotBlank(message = "Status is required")
            String status

    ) {
    }

    public record Response(
            UUID id,
            UUID userId,
            UUID productId,
            String productTitle,
            String status,
            BigDecimal totalAmount,
            String currency,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
