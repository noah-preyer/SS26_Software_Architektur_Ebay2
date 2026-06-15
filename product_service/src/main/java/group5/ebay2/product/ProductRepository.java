package group5.ebay2.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCategory(String category);
    List<Product> findBySellerId(UUID sellerId);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity - 1 WHERE p.id = :id AND p.quantity > 0")
    int decrementQuantity(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity + 1 WHERE p.id = :id")
    void incrementQuantity(@Param("id") UUID id);
}
