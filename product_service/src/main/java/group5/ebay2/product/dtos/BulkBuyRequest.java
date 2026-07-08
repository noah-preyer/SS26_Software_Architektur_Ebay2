package group5.ebay2.product.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkBuyRequest(
        @NotNull Long buyerId,
        @NotEmpty List<@NotNull Long> productIds
) {}
