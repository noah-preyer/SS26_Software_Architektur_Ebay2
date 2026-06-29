package group5.ebay2.product.dtos;

import java.math.BigDecimal;

public record BuyProductResponse(
        Long productId,
        String productTitle,
        BigDecimal price,
        int remainingQuantity,
        Long orderId,
        String orderStatus
) {}