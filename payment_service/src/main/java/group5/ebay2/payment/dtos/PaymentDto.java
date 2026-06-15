package group5.ebay2.payment.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentDto {

    public record ProcessRequest(

            @NotNull(message = "Order ID is required")
            UUID orderId,

            @NotBlank(message = "Product ID is required")
            String productId,

            @NotNull(message = "User ID is required")
            UUID userId,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
            BigDecimal amount,

            @NotBlank(message = "Currency is required")
            @Size(min = 3, max = 3, message = "Currency must be 3 characters")
            String currency,

            @NotBlank(message = "Payment method type is required")
            String paymentMethodType

    ) {
    }

    public record RefundRequest(
            String reason
    ) {
    }

    public record Response(
            UUID id,
            UUID orderId,
            String productId,
            UUID userId,
            BigDecimal amount,
            String currency,
            String status,
            String paymentMethodType,
            String transactionId,
            Instant paidAt
    ) {
    }
}
