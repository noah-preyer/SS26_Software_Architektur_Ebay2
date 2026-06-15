package group5.ebay2.payment.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import group5.ebay2.payment.Order;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
