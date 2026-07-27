package group5.ebay2.order;

import group5.ebay2.order.dtos.OrderDto;
import group5.ebay2.order.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String DEFAULT_CURRENCY = "EUR";

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
    public List<OrderDto.CheckoutItemResult> checkout(OrderDto.CheckoutRequest request) {
        Long userId = request.userId();
        List<OrderDto.CheckoutItem> items = request.items();

        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product list must not be empty");
        }

        List<ProductDto> products = new ArrayList<>();
        for (OrderDto.CheckoutItem item : items) {
            ProductDto product = productServiceClient.getProduct(item.productId());
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + item.productId());
            }
            if (!"AVAILABLE".equals(product.status())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Product is already sold: " + item.productId());
            }
            if (product.sellerId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You cannot buy your own product: " + item.productId());
            }
            products.add(product);
        }

        List<Long> reservedIds = new ArrayList<>();
        try {
            for (ProductDto product : products) {
                productServiceClient.reserveProduct(product.id());
                reservedIds.add(product.id());
            }
        } catch (Exception e) {
            for (Long id : reservedIds) {
                try {
                    productServiceClient.unreserveProduct(id);
                } catch (Exception ex) {
                    log.warn("Failed to unreserve product {} during rollback: {}", id, ex.getMessage());
                }
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Failed to reserve products: " + e.getMessage());
        }

        Order order = new Order(userId, DEFAULT_CURRENCY);
        for (int i = 0; i < products.size(); i++) {
            ProductDto product = products.get(i);
            OrderDto.CheckoutItem item = items.get(i);
            order.addItem(new OrderItem(
                    product.id(), product.title(), item.quantity(), product.price(), order
            ));
        }
        Order saved = orderRepository.save(order);
        log.info("Created order {} for user {} with {} items", saved.getId(), userId, saved.getItems().size());

        emailServiceClient.sendOrderComplete(userId, saved);

        List<OrderDto.CheckoutItemResult> results = new ArrayList<>();
        for (ProductDto product : products) {
            results.add(new OrderDto.CheckoutItemResult(
                    product.id(), product.title(), product.price(),
                    saved.getId(), saved.getStatus()));
        }
        return results;
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