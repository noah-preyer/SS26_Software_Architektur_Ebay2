package group5.ebay2.order;

import group5.ebay2.order.dtos.OrderDto;
import group5.ebay2.order.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final EmailServiceClient emailServiceClient;
    private final ProductServiceClient productServiceClient;

    public OrderService(OrderRepository orderRepository, 
                        EmailServiceClient emailServiceClient,
                        ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.emailServiceClient = emailServiceClient;
        this.productServiceClient = productServiceClient;
    }

    @Transactional
    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
        log.info("Creating order for user: {} with {} items", request.userId(), request.items().size());

        Order order = new Order(request.userId(), request.currency());

        for (OrderDto.CreateOrderItem item : request.items()) {
            ProductDto product = productServiceClient.getProduct(item.productId());
            if (product == null) {
                throw new OrderExceptions.OrderNotFoundException("Product not found: " + item.productId());
            }
            order.addItem(new OrderItem(
                    product.id(), product.title(), item.quantity(), product.price(), order
            ));
        }

        Order saved = orderRepository.save(order);
        log.info("Created order: {} with {} items, total: {} {}", saved.getId(), saved.getItems().size(),
                saved.getTotalAmount(), saved.getCurrency());

        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.Response markOrderPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId));
        order.markPaid();
        Order saved = orderRepository.save(order);
        log.info("Order: {} marked as PAID", saved.getId());

        String products = saved.getItems().stream()
                .map(i -> "  • " + i.getProductTitle() + " (x" + i.getQuantity() + ") — " + i.getPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity())) + " " + saved.getCurrency())
                .collect(java.util.stream.Collectors.joining("\n"));

        emailServiceClient.sendOrderComplete(
                saved.getUserId(),
                saved.getId().toString(),
                products,
                saved.getTotalAmount().toString(),
                saved.getCurrency()
        );

        return toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto.Response getOrder(Long orderId) {
        return toOrderResponse(orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId)));
    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    private OrderDto.Response toOrderResponse(Order order) {
        Long firstProductId = order.getItems().isEmpty() ? null : order.getItems().get(0).getProductId();
        return new OrderDto.Response(
                order.getId(), order.getUserId(), firstProductId, order.getStatus(),
                order.getTotalAmount(), order.getCurrency(),
                order.getItems().stream().map(i -> new OrderDto.OrderItemResponse(
                        i.getProductId(), i.getProductTitle(), i.getQuantity(), i.getPrice()
                )).toList(),
                order.getCreatedAt(), order.getUpdatedAt()
        );
    }
}
