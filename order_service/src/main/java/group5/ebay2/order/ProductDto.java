package group5.ebay2.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String title,
        String description,
        BigDecimal price,
        String category,
        UUID sellerId,
        int quantity,
        String status,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
}
