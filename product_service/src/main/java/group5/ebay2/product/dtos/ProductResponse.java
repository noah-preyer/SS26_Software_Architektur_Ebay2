package group5.ebay2.product.dtos;

import group5.ebay2.product.Product;
import group5.ebay2.product.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String category,
        Long sellerId,
        String sellerUsername,
        ProductStatus status,
        LocalDateTime createdAt,
        List<String> imageUrls
) {
    public static ProductResponse from(Product product, String sellerUsername) {
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getSellerId(),
                sellerUsername,
                product.getStatus(),
                product.getCreatedAt(),
                product.getImageUrls()
        );
    }
}