package group5.ebay2.payment;

import group5.ebay2.payment.dtos.PaymentDto;
import group5.ebay2.payment.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private UUID userId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    void getPayment_shouldReturnPayment() {
        Payment payment = new Payment(orderId, "PRODUCT-100", userId, new BigDecimal("49.99"), "USD", "CREDIT_CARD");
        payment.markCompleted("txn_123");
        Payment saved = paymentRepository.save(payment);

        PaymentDto.Response found = paymentService.getPayment(saved.getId());

        assertThat(found.status()).isEqualTo("COMPLETED");
        assertThat(found.transactionId()).isEqualTo("txn_123");
    }

    @Test
    void getPayment_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> paymentService.getPayment(UUID.randomUUID()))
                .isInstanceOf(PaymentExceptions.PaymentNotFoundException.class);
    }

    @Test
    void getPaymentsByOrder_shouldReturnAllPayments() {
        Payment payment1 = new Payment(orderId, "PRODUCT-200", userId, new BigDecimal("25.00"), "USD", "CREDIT_CARD");
        payment1.markCompleted("txn_001");
        paymentRepository.save(payment1);

        Payment payment2 = new Payment(orderId, "PRODUCT-200", userId, new BigDecimal("15.00"), "USD", "PAYPAL");
        payment2.markCompleted("txn_002");
        paymentRepository.save(payment2);

        List<PaymentDto.Response> payments = paymentService.getPaymentsByOrder(orderId);

        assertThat(payments).hasSize(2);
    }

    @Test
    void refundPayment_shouldMarkPaymentRefunded() {
        Payment payment = new Payment(orderId, "PRODUCT-300", userId, new BigDecimal("99.99"), "USD", "CREDIT_CARD");
        payment.markCompleted("txn_003");
        Payment saved = paymentRepository.save(payment);

        PaymentDto.Response refunded = paymentService.refundPayment(saved.getId(),
                new PaymentDto.RefundRequest("Customer request"));

        assertThat(refunded.status()).isEqualTo("REFUNDED");
    }

    @Test
    void refundPayment_shouldThrowOnAlreadyRefunded() {
        Payment payment = new Payment(orderId, "PRODUCT-400", userId, new BigDecimal("5.00"), "USD", "CREDIT_CARD");
        payment.markCompleted("txn_004");
        Payment saved = paymentRepository.save(payment);

        paymentService.refundPayment(saved.getId(), null);

        assertThatThrownBy(() -> paymentService.refundPayment(saved.getId(), null))
                .isInstanceOf(PaymentExceptions.PaymentAlreadyRefundedException.class);
    }

    @Test
    void refundPayment_shouldThrowOnPendingPayment() {
        Payment payment = new Payment(orderId, "PRODUCT-500", userId, new BigDecimal("20.00"), "USD", "CREDIT_CARD");
        Payment saved = paymentRepository.save(payment);

        assertThatThrownBy(() -> paymentService.refundPayment(saved.getId(), null))
                .isInstanceOf(PaymentExceptions.InvalidPaymentStateException.class);
    }
}
