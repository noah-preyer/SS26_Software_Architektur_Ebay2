package group5.ebay2.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findBySellerId(Long sellerId);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity - 1 WHERE p.id = :id AND p.quantity > 0")
    int decrementQuantity(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity + 1 WHERE p.id = :id")
    void incrementQuantity(@Param("id") Long id);
}
