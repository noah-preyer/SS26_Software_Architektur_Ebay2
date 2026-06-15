package group5.ebay2.payment;

public record OrderDto(
        java.util.UUID id,
        java.util.UUID userId,
        String productId,
        String status,
        java.math.BigDecimal totalAmount,
        String currency,
        java.time.Instant createdAt,
        java.time.Instant updatedAt
) {
}
