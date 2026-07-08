package group5.ebay2.order.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDto {

    public record CreateOrderItem(
            @NotNull(message = "Product ID is required")
            Long productId,

            @NotNull(message = "Quantity is required")
            Integer quantity
    ) {}

    public record CreateRequest(
            @NotNull(message = "User ID is required")
            Long userId,

            @NotEmpty(message = "At least one product is required")
            List<CreateOrderItem> items,

            @NotBlank(message = "Currency is required")
            @Size(min = 3, max = 3, message = "Currency must be 3 characters")
            String currency
    ) {}

    public record OrderItemResponse(
            Long productId,
            String productTitle,
            Integer quantity,
            BigDecimal price
    ) {}

    public record Response(
            Long id,
            Long userId,
            Long productId,
            String status,
            BigDecimal totalAmount,
            String currency,
            List<OrderItemResponse> items,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
