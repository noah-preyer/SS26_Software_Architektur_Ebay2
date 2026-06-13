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
                new OrderDto.CreateRequest(userId, new BigDecimal("99.99"), "USD"));

        assertThat(response.id()).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo("CREATED");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void getOrder_shouldReturnOrder() {
        OrderDto.Response created = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("50.00"), "EUR"));

        OrderDto.Response found = paymentService.getOrder(created.id());

        assertThat(found.status()).isEqualTo("CREATED");
    }

    @Test
    void getOrder_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> paymentService.getOrder(UUID.randomUUID()))
                .isInstanceOf(PaymentExceptions.OrderNotFoundException.class);
    }

    @Test
    void updateOrderStatus_shouldTransition() {
        OrderDto.Response created = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("30.00"), "USD"));

        OrderDto.Response shipped = paymentService.updateOrderStatus(created.id(),
                new OrderDto.StatusUpdateRequest("SHIPPED"));
        assertThat(shipped.status()).isEqualTo("SHIPPED");

        OrderDto.Response delivered = paymentService.updateOrderStatus(created.id(),
                new OrderDto.StatusUpdateRequest("DELIVERED"));
        assertThat(delivered.status()).isEqualTo("DELIVERED");
    }

    @Test
    void updateOrderStatus_shouldThrowOnInvalidStatus() {
        OrderDto.Response created = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("15.00"), "USD"));

        assertThatThrownBy(() -> paymentService.updateOrderStatus(created.id(),
                new OrderDto.StatusUpdateRequest("INVALID")))
                .isInstanceOf(PaymentExceptions.InvalidOrderStateException.class);
    }

    @Test
    void getOrdersByUser_shouldReturnUserOrders() {
        paymentService.createOrder(new OrderDto.CreateRequest(userId, new BigDecimal("10.00"), "USD"));
        paymentService.createOrder(new OrderDto.CreateRequest(userId, new BigDecimal("20.00"), "USD"));

        List<OrderDto.Response> orders = paymentService.getOrdersByUser(userId);

        assertThat(orders).hasSize(2);
    }

    @Test
    void processPayment_shouldCompleteAndMarkOrderPaid() {
        OrderDto.Response order = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("49.99"), "USD"));

        PaymentDto.Response payment = paymentService.processPayment(
                new PaymentDto.ProcessRequest(order.id(), "PRODUCT-100", userId, new BigDecimal("49.99"), "USD", "CREDIT_CARD"));

        assertThat(payment.status()).isEqualTo("COMPLETED");
        assertThat(payment.transactionId()).isNotNull();

        OrderDto.Response updatedOrder = paymentService.getOrder(order.id());
        assertThat(updatedOrder.status()).isEqualTo("PAID");
    }

    @Test
    void processPayment_shouldThrowOnMissingOrder() {
        assertThatThrownBy(() -> paymentService.processPayment(
                new PaymentDto.ProcessRequest(UUID.randomUUID(), "PRODUCT-MISSING", userId, new BigDecimal("10.00"), "USD", "CREDIT_CARD")))
                .isInstanceOf(PaymentExceptions.OrderNotFoundException.class);
    }

    @Test
    void processPayment_shouldThrowOnAlreadyPaidOrder() {
        OrderDto.Response order = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("25.00"), "USD"));
        paymentService.processPayment(
                new PaymentDto.ProcessRequest(order.id(), "PRODUCT-200", userId, new BigDecimal("25.00"), "USD", "PAYPAL"));

        assertThatThrownBy(() -> paymentService.processPayment(
                new PaymentDto.ProcessRequest(order.id(), "PRODUCT-200", userId, new BigDecimal("25.00"), "USD", "PAYPAL")))
                .isInstanceOf(PaymentExceptions.InvalidOrderStateException.class);
    }

    @Test
    void getPaymentsByOrder_shouldReturnAllPayments() {
        OrderDto.Response order = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("40.00"), "USD"));
        paymentService.processPayment(
                new PaymentDto.ProcessRequest(order.id(), "PRODUCT-300", userId, new BigDecimal("25.00"), "USD", "CREDIT_CARD"));
        paymentService.processPayment(
                new PaymentDto.ProcessRequest(order.id(), "PRODUCT-300", userId, new BigDecimal("15.00"), "USD", "PAYPAL"));

        List<PaymentDto.Response> payments = paymentService.getPaymentsByOrder(order.id());

        assertThat(payments).hasSize(2);
    }

    @Test
    void refundPayment_shouldMarkPaymentAndOrderRefunded() {
        OrderDto.Response order = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("99.99"), "USD"));
        PaymentDto.Response payment = paymentService.processPayment(
                new PaymentDto.ProcessRequest(order.id(), "PRODUCT-400", userId, new BigDecimal("99.99"), "USD", "CREDIT_CARD"));

        PaymentDto.Response refunded = paymentService.refundPayment(payment.id(),
                new PaymentDto.RefundRequest("Customer request"));

        assertThat(refunded.status()).isEqualTo("REFUNDED");
        assertThat(paymentService.getOrder(order.id()).status()).isEqualTo("REFUNDED");
    }

    @Test
    void refundPayment_shouldThrowOnAlreadyRefunded() {
        OrderDto.Response order = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("5.00"), "USD"));
        PaymentDto.Response payment = paymentService.processPayment(
                new PaymentDto.ProcessRequest(order.id(), "PRODUCT-500", userId, new BigDecimal("5.00"), "USD", "CREDIT_CARD"));

        paymentService.refundPayment(payment.id(), null);

        assertThatThrownBy(() -> paymentService.refundPayment(payment.id(), null))
                .isInstanceOf(PaymentExceptions.PaymentAlreadyRefundedException.class);
    }

    @Test
    void refundPayment_shouldThrowOnPendingPayment() {
        OrderDto.Response order = paymentService.createOrder(
                new OrderDto.CreateRequest(userId, new BigDecimal("20.00"), "USD"));
        Payment payment = new Payment(order.id(), "PRODUCT-600", userId, new BigDecimal("20.00"), "USD", "CREDIT_CARD");
        Payment saved = paymentRepository.save(payment);

        assertThatThrownBy(() -> paymentService.refundPayment(saved.getId(), null))
                .isInstanceOf(PaymentExceptions.InvalidPaymentStateException.class);
    }
}
