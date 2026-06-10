package group5.ebay2.payment.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import group5.ebay2.payment.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByOrderIdOrderByCreatedAtDesc(String orderId);
}
