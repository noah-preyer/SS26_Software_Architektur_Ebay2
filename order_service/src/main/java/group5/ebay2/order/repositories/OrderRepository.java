package group5.ebay2.order.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import group5.ebay2.order.Order;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatusAndUpdatedAtBefore(String status, Instant updatedAt);
}
