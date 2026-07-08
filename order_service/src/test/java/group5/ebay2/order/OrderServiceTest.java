package group5.ebay2.order;

import group5.ebay2.order.dtos.OrderDto;
import group5.ebay2.order.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    private OrderDto.CreateRequest singleItemRequest(Long productId, String currency) {
        return new OrderDto.CreateRequest(userId, List.of(new OrderDto.CreateOrderItem(productId, 1)), currency);
    }

    @Test
    void createOrder_shouldCreateWithCreatedStatus() {
        Long productId = nextId();

        OrderDto.Response response = orderService.createOrder(singleItemRequest(productId, "USD"));

        assertThat(response.id()).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo("CREATED");
    }

    @Test
    void getOrder_shouldReturnOrder() {
        Long productId = nextId();

        OrderDto.Response created = orderService.createOrder(singleItemRequest(productId, "EUR"));

        OrderDto.Response found = orderService.getOrder(created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.userId()).isEqualTo(userId);
        assertThat(found.status()).isEqualTo("CREATED");
        assertThat(found.items()).isNotEmpty();
    }

    @Test
    void getOrder_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(OrderExceptions.OrderNotFoundException.class);
    }

    @Test
    void getOrdersByUser_shouldReturnUserOrders() {
        orderService.createOrder(singleItemRequest(nextId(), "USD"));
        orderService.createOrder(singleItemRequest(nextId(), "USD"));

        List<OrderDto.Response> orders = orderService.getOrdersByUser(userId);

        assertThat(orders).hasSize(2);
    }
}
