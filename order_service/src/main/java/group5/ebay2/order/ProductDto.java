package group5.ebay2.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDto(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String category,
        Long sellerId,
        int quantity,
        String status,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
}
