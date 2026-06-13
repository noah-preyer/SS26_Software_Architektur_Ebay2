package group5.ebay2.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCategory(String category);
    List<Product> findBySellerId(UUID sellerId);
}
