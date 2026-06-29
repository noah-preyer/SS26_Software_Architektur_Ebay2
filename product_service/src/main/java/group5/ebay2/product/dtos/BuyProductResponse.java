package group5.ebay2.product.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record BuyProductResponse(
        Long productId,
        String productTitle,
        BigDecimal price,
        int remainingQuantity,
        UUID orderId,
        String orderStatus
) {}