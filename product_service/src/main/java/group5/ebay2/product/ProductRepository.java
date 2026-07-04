package group5.ebay2.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByCategoryAndStatus(String category, ProductStatus status);
    List<Product> findBySellerId(Long sellerId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE products SET status = 'SOLD' WHERE id = :id AND status = 'AVAILABLE'", nativeQuery = true)
    int markAsSold(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE products SET status = 'AVAILABLE' WHERE id = :id", nativeQuery = true)
    void markAsAvailable(@Param("id") Long id);
}