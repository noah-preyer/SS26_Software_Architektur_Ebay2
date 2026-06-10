package group5.ebay2.payment;

import group5.ebay2.payment.dtos.OrderDto;
import group5.ebay2.payment.dtos.PaymentDto;
import group5.ebay2.payment.repositories.OrderRepository;
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
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void createOrder_shouldCreateWithCreatedStatus() {
        OrderDto.Response response = paymentService.createOrder(
                new OrderDto.CreateRequest("ORDER-001", userId, new BigDecimal("99.99"), "USD"));

        assertThat(response.id()).isEqualTo("ORDER-001");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo("CREATED");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void createOrder_shouldThrowOnDuplicate() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-002", userId, new BigDecimal("10.00"), "USD"));

        assertThatThrownBy(() -> paymentService.createOrder(
                new OrderDto.CreateRequest("ORDER-002", userId, new BigDecimal("20.00"), "USD")))
                .isInstanceOf(PaymentExceptions.OrderAlreadyExistsException.class);
    }

    @Test
    void getOrder_shouldReturnOrder() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-003", userId, new BigDecimal("50.00"), "EUR"));

        OrderDto.Response found = paymentService.getOrder("ORDER-003");

        assertThat(found.status()).isEqualTo("CREATED");
    }

    @Test
    void getOrder_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> paymentService.getOrder("NONEXISTENT"))
                .isInstanceOf(PaymentExceptions.OrderNotFoundException.class);
    }

    @Test
    void updateOrderStatus_shouldTransition() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-004", userId, new BigDecimal("30.00"), "USD"));

        OrderDto.Response shipped = paymentService.updateOrderStatus("ORDER-004",
                new OrderDto.StatusUpdateRequest("SHIPPED"));
        assertThat(shipped.status()).isEqualTo("SHIPPED");

        OrderDto.Response delivered = paymentService.updateOrderStatus("ORDER-004",
                new OrderDto.StatusUpdateRequest("DELIVERED"));
        assertThat(delivered.status()).isEqualTo("DELIVERED");
    }

    @Test
    void updateOrderStatus_shouldThrowOnInvalidStatus() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-005", userId, new BigDecimal("15.00"), "USD"));

        assertThatThrownBy(() -> paymentService.updateOrderStatus("ORDER-005",
                new OrderDto.StatusUpdateRequest("INVALID")))
                .isInstanceOf(PaymentExceptions.InvalidOrderStateException.class);
    }

    @Test
    void getOrdersByUser_shouldReturnUserOrders() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-006", userId, new BigDecimal("10.00"), "USD"));
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-007", userId, new BigDecimal("20.00"), "USD"));

        List<OrderDto.Response> orders = paymentService.getOrdersByUser(userId);

        assertThat(orders).hasSize(2);
    }

    @Test
    void processPayment_shouldCompleteAndMarkOrderPaid() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-100", userId, new BigDecimal("49.99"), "USD"));

        PaymentDto.Response payment = paymentService.processPayment(
                new PaymentDto.ProcessRequest("ORDER-100", userId, new BigDecimal("49.99"), "USD", "CREDIT_CARD"));

        assertThat(payment.status()).isEqualTo("COMPLETED");
        assertThat(payment.transactionId()).isNotNull();

        OrderDto.Response order = paymentService.getOrder("ORDER-100");
        assertThat(order.status()).isEqualTo("PAID");
    }

    @Test
    void processPayment_shouldThrowOnMissingOrder() {
        assertThatThrownBy(() -> paymentService.processPayment(
                new PaymentDto.ProcessRequest("ORDER-MISSING", userId, new BigDecimal("10.00"), "USD", "CREDIT_CARD")))
                .isInstanceOf(PaymentExceptions.OrderNotFoundException.class);
    }

    @Test
    void processPayment_shouldThrowOnAlreadyPaidOrder() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-200", userId, new BigDecimal("25.00"), "USD"));
        paymentService.processPayment(
                new PaymentDto.ProcessRequest("ORDER-200", userId, new BigDecimal("25.00"), "USD", "PAYPAL"));

        assertThatThrownBy(() -> paymentService.processPayment(
                new PaymentDto.ProcessRequest("ORDER-200", userId, new BigDecimal("25.00"), "USD", "PAYPAL")))
                .isInstanceOf(PaymentExceptions.InvalidOrderStateException.class);
    }

    @Test
    void getPaymentsByOrder_shouldReturnAllPayments() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-300", userId, new BigDecimal("40.00"), "USD"));
        paymentService.processPayment(
                new PaymentDto.ProcessRequest("ORDER-300", userId, new BigDecimal("25.00"), "USD", "CREDIT_CARD"));
        paymentService.processPayment(
                new PaymentDto.ProcessRequest("ORDER-300", userId, new BigDecimal("15.00"), "USD", "PAYPAL"));

        List<PaymentDto.Response> payments = paymentService.getPaymentsByOrder("ORDER-300");

        assertThat(payments).hasSize(2);
    }

    @Test
    void refundPayment_shouldMarkPaymentAndOrderRefunded() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-400", userId, new BigDecimal("99.99"), "USD"));
        PaymentDto.Response payment = paymentService.processPayment(
                new PaymentDto.ProcessRequest("ORDER-400", userId, new BigDecimal("99.99"), "USD", "CREDIT_CARD"));

        PaymentDto.Response refunded = paymentService.refundPayment(payment.id(),
                new PaymentDto.RefundRequest("Customer request"));

        assertThat(refunded.status()).isEqualTo("REFUNDED");
        assertThat(paymentService.getOrder("ORDER-400").status()).isEqualTo("REFUNDED");
    }

    @Test
    void refundPayment_shouldThrowOnAlreadyRefunded() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-500", userId, new BigDecimal("5.00"), "USD"));
        PaymentDto.Response payment = paymentService.processPayment(
                new PaymentDto.ProcessRequest("ORDER-500", userId, new BigDecimal("5.00"), "USD", "CREDIT_CARD"));

        paymentService.refundPayment(payment.id(), null);

        assertThatThrownBy(() -> paymentService.refundPayment(payment.id(), null))
                .isInstanceOf(PaymentExceptions.PaymentAlreadyRefundedException.class);
    }

    @Test
    void refundPayment_shouldThrowOnPendingPayment() {
        paymentService.createOrder(new OrderDto.CreateRequest("ORDER-600", userId, new BigDecimal("20.00"), "USD"));
        Payment payment = new Payment("ORDER-600", userId, new BigDecimal("20.00"), "USD", "CREDIT_CARD");
        Payment saved = paymentRepository.save(payment);

        assertThatThrownBy(() -> paymentService.refundPayment(saved.getId(), null))
                .isInstanceOf(PaymentExceptions.InvalidPaymentStateException.class);
    }
}
