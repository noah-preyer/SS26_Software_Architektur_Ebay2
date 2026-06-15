package group5.ebay2.product.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BuyProductResponse(
        UUID productId,
        String productTitle,
        BigDecimal price,
        int remainingQuantity,
        UUID orderId,
        String orderStatus,
        UUID paymentId,
        String paymentStatus,
        String transactionId,
        Instant paidAt
) {}
