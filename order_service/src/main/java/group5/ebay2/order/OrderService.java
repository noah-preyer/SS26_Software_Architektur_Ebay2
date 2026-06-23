package group5.ebay2.order;

import group5.ebay2.order.dtos.OrderDto;
import group5.ebay2.order.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final Set<String> VALID_TRANSITIONS = Set.of(
            "SHIPPED", "DELIVERED", "CANCELLED"
    );

    private final OrderRepository orderRepository;
    private final EmailServiceClient emailServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final ProductServiceClient productServiceClient;

    public OrderService(OrderRepository orderRepository, 
                        EmailServiceClient emailServiceClient,
                        PaymentServiceClient paymentServiceClient,
                        ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.emailServiceClient = emailServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.productServiceClient = productServiceClient;
    }

    @Transactional
    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
        log.info("Creating order for user: {} product: {}", request.userId(), request.productId());

        ProductDto product = productServiceClient.getProduct(request.productId());
        if (product == null) {
            throw new OrderExceptions.OrderNotFoundException("Product not found: " + request.productId());
        }

        Order order = new Order(
                request.userId(), 
                product.id(), 
                product.title(), 
                product.price(), 
                request.currency()
        );
        Order saved = orderRepository.save(order);
        log.info("Created order: {} status: {} product: {}", saved.getId(), saved.getStatus(), product.title());

        return toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto.Response getOrder(UUID orderId) {
        return toOrderResponse(orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId)));
    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderDto.Response updateOrderStatus(UUID orderId, OrderDto.StatusUpdateRequest request) {
        log.info("Updating order: {} status to: {}", orderId, request.status());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId));

        if (!VALID_TRANSITIONS.contains(request.status())) {
            throw new OrderExceptions.InvalidOrderStateException(
                    "Invalid status transition: " + request.status());
        }

        switch (request.status()) {
            case "SHIPPED" -> {
                order.markShipped();
                emailServiceClient.sendShippingConfirmation(
                        order.getUserId(),
                        order.getId().toString(),
                        order.getProductTitle()
                );
            }
            case "DELIVERED" -> {
                order.markDelivered();
                emailServiceClient.sendDeliveryConfirmation(
                        order.getUserId(),
                        order.getId().toString(),
                        order.getProductTitle()
                );
            }
            case "CANCELLED" -> order.markCancelled();
        }

        Order saved = orderRepository.save(order);
        log.info("Order: {} status updated to: {}", saved.getId(), saved.getStatus());

        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.Response markOrderPaid(UUID orderId) {
        log.info("Marking order: {} as paid", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId));

        order.markPaid();
        Order saved = orderRepository.save(order);
        log.info("Order: {} status updated to: {}", saved.getId(), saved.getStatus());

        emailServiceClient.sendPaymentConfirmation(
                saved.getUserId(),
                saved.getId().toString(),
                saved.getProductTitle(),
                saved.getTotalAmount().toString(),
                saved.getCurrency()
        );

        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.Response markOrderShipped(UUID orderId) {
        log.info("Marking order: {} as shipped", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId));

        order.markShipped();
        Order saved = orderRepository.save(order);
        log.info("Order: {} status updated to: {}", saved.getId(), saved.getStatus());

        emailServiceClient.sendShippingConfirmation(
                saved.getUserId(),
                saved.getId().toString(),
                saved.getProductTitle()
        );

        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.Response markOrderDelivered(UUID orderId) {
        log.info("Marking order: {} as delivered", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId));

        order.markDelivered();
        Order saved = orderRepository.save(order);
        log.info("Order: {} status updated to: {}", saved.getId(), saved.getStatus());

        emailServiceClient.sendDeliveryConfirmation(
                saved.getUserId(),
                saved.getId().toString(),
                saved.getProductTitle()
        );

        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.Response refundOrder(UUID orderId) {
        log.info("Processing refund for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId));

        paymentServiceClient.refundPayment(orderId);

        order.markRefunded();
        Order saved = orderRepository.save(order);
        log.info("Order: {} status updated to: {}", saved.getId(), saved.getStatus());

        emailServiceClient.sendRefundConfirmation(
                saved.getUserId(),
                saved.getId().toString(),
                saved.getProductTitle(),
                saved.getTotalAmount().toString(),
                saved.getCurrency()
        );

        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.Response markOrderRefunded(UUID orderId) {
        log.info("Marking order: {} as refunded", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId));

        order.markRefunded();
        Order saved = orderRepository.save(order);
        log.info("Order: {} status updated to: {}", saved.getId(), saved.getStatus());

        emailServiceClient.sendRefundConfirmation(
                saved.getUserId(),
                saved.getId().toString(),
                saved.getProductTitle(),
                saved.getTotalAmount().toString(),
                saved.getCurrency()
        );

        return toOrderResponse(saved);
    }

    private OrderDto.Response toOrderResponse(Order order) {
        return new OrderDto.Response(
                order.getId(), order.getUserId(), order.getProductId(), order.getProductTitle(),
                order.getStatus(), order.getTotalAmount(), order.getCurrency(),
                order.getCreatedAt(), order.getUpdatedAt()
        );
    }
}
