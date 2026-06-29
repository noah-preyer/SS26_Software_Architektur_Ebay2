package group5.ebay2.order;

import group5.ebay2.order.dtos.OrderDto;
import group5.ebay2.order.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderServiceTest {

    private static final AtomicLong ID_SEQUENCE = new AtomicLong(1);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    private Long userId;

    private static Long nextId() {
        return ID_SEQUENCE.getAndIncrement();
    }

    @BeforeEach
    void setUp() {
        userId = nextId();
    }

    @Test
    void createOrder_shouldCreateWithCreatedStatus() {
        Long productId = nextId();

        OrderDto.Response response = orderService.createOrder(
                new OrderDto.CreateRequest(userId, productId, "USD"));

        assertThat(response.id()).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.status()).isEqualTo("CREATED");
    }

    @Test
    void getOrder_shouldReturnOrder() {
        Long productId = nextId();

        OrderDto.Response created = orderService.createOrder(
                new OrderDto.CreateRequest(userId, productId, "EUR"));

        OrderDto.Response found = orderService.getOrder(created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.userId()).isEqualTo(userId);
        assertThat(found.productId()).isEqualTo(productId);
        assertThat(found.status()).isEqualTo("CREATED");
    }

    @Test
    void getOrder_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> orderService.getOrder(UUID.randomUUID()))
                .isInstanceOf(OrderExceptions.OrderNotFoundException.class);
    }

    @Test
    void updateOrderStatus_shouldTransition() {
        Long productId = nextId();

        OrderDto.Response created = orderService.createOrder(
                new OrderDto.CreateRequest(userId, productId, "USD"));

        OrderDto.Response shipped = orderService.updateOrderStatus(
                created.id(),
                new OrderDto.StatusUpdateRequest("SHIPPED"));

        assertThat(shipped.status()).isEqualTo("SHIPPED");

        OrderDto.Response delivered = orderService.updateOrderStatus(
                created.id(),
                new OrderDto.StatusUpdateRequest("DELIVERED"));

        assertThat(delivered.status()).isEqualTo("DELIVERED");
    }

    @Test
    void updateOrderStatus_shouldThrowOnInvalidStatus() {
        Long productId = nextId();

        OrderDto.Response created = orderService.createOrder(
                new OrderDto.CreateRequest(userId, productId, "USD"));

        assertThatThrownBy(() -> orderService.updateOrderStatus(
                created.id(),
                new OrderDto.StatusUpdateRequest("INVALID")))
                .isInstanceOf(OrderExceptions.InvalidOrderStateException.class);
    }

    @Test
    void getOrdersByUser_shouldReturnUserOrders() {
        orderService.createOrder(
                new OrderDto.CreateRequest(userId, nextId(), "USD"));

        orderService.createOrder(
                new OrderDto.CreateRequest(userId, nextId(), "USD"));

        List<OrderDto.Response> orders = orderService.getOrdersByUser(userId);

        assertThat(orders).hasSize(2);
    }

    @Test
    void markOrderPaid_shouldUpdateStatus() {
        Long productId = nextId();

        OrderDto.Response created = orderService.createOrder(
                new OrderDto.CreateRequest(userId, productId, "USD"));

        OrderDto.Response paid = orderService.markOrderPaid(created.id());

        assertThat(paid.status()).isEqualTo("PAID");
    }

    @Test
    void markOrderPaid_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> orderService.markOrderPaid(UUID.randomUUID()))
                .isInstanceOf(OrderExceptions.OrderNotFoundException.class);
    }

    @Test
    void markOrderRefunded_shouldUpdateStatus() {
        Long productId = nextId();

        OrderDto.Response created = orderService.createOrder(
                new OrderDto.CreateRequest(userId, productId, "USD"));

        orderService.markOrderPaid(created.id());

        OrderDto.Response refunded = orderService.markOrderRefunded(created.id());

        assertThat(refunded.status()).isEqualTo("REFUNDED");
    }

    @Test
    void markOrderRefunded_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> orderService.markOrderRefunded(UUID.randomUUID()))
                .isInstanceOf(OrderExceptions.OrderNotFoundException.class);
    }
}