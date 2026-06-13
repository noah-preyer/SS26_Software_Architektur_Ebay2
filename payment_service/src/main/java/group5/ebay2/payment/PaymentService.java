package group5.ebay2.payment;

import group5.ebay2.payment.dtos.OrderDto;
import group5.ebay2.payment.dtos.PaymentDto;
import group5.ebay2.payment.repositories.OrderRepository;
import group5.ebay2.payment.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private static final Set<String> VALID_TRANSITIONS = Set.of(
            "SHIPPED", "DELIVERED", "CANCELLED"
    );

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentDto.Response processPayment(PaymentDto.ProcessRequest request) {
        log.info("Processing payment for order: {} amount: {} {}", request.orderId(), request.amount(), request.currency());

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new PaymentExceptions.OrderNotFoundException(
                        "Order not found: " + request.orderId()));

        if (!"CREATED".equals(order.getStatus())) {
            throw new PaymentExceptions.InvalidOrderStateException(
                    "Cannot pay for order with status: " + order.getStatus());
        }

        Payment payment = new Payment(
                request.orderId(), request.productId(), request.userId(),
                request.amount(), request.currency(), request.paymentMethodType()
        );
        payment.markCompleted("txn_" + UUID.randomUUID().toString().replace("-", ""));

        order.markPaid();

        paymentRepository.save(payment);
        orderRepository.save(order);

        log.info("Payment completed id: {} for order: {}", payment.getId(), order.getId());

        return toPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto.Response getPayment(UUID id) {
        return toPaymentResponse(paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentExceptions.PaymentNotFoundException("Payment not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.Response> getPaymentsByOrder(UUID orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Transactional
    public PaymentDto.Response refundPayment(UUID id, PaymentDto.RefundRequest request) {
        log.info("Refunding payment: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentExceptions.PaymentNotFoundException("Payment not found: " + id));

        if ("REFUNDED".equals(payment.getStatus())) {
            throw new PaymentExceptions.PaymentAlreadyRefundedException("Payment already refunded: " + id);
        }
        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new PaymentExceptions.InvalidPaymentStateException(
                    "Cannot refund payment with status: " + payment.getStatus());
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new PaymentExceptions.OrderNotFoundException(
                        "Order not found: " + payment.getOrderId()));

        payment.markRefunded();
        order.markRefunded();

        paymentRepository.save(payment);
        orderRepository.save(order);

        String reason = request != null ? request.reason() : null;
        log.info("Refunded payment id: {} order: {} reason: {}", id, order.getId(), reason);

        return toPaymentResponse(payment);
    }

    @Transactional
    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
        log.info("Creating order for user: {}", request.userId());

        Order order = new Order(request.userId(), request.totalAmount(), request.currency());
        Order saved = orderRepository.save(order);
        log.info("Created order: {} status: {}", saved.getId(), saved.getStatus());

        return toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto.Response getOrder(UUID orderId) {
        return toOrderResponse(orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentExceptions.OrderNotFoundException("Order not found: " + orderId)));
    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> getOrdersByUser(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderDto.Response updateOrderStatus(UUID orderId, OrderDto.StatusUpdateRequest request) {
        log.info("Updating order: {} status to: {}", orderId, request.status());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentExceptions.OrderNotFoundException("Order not found: " + orderId));

        if (!VALID_TRANSITIONS.contains(request.status())) {
            throw new PaymentExceptions.InvalidOrderStateException(
                    "Invalid status transition: " + request.status());
        }

        switch (request.status()) {
            case "SHIPPED" -> order.markShipped();
            case "DELIVERED" -> order.markDelivered();
            case "CANCELLED" -> order.markCancelled();
        }

        Order saved = orderRepository.save(order);
        log.info("Order: {} status updated to: {}", saved.getId(), saved.getStatus());

        return toOrderResponse(saved);
    }

    private PaymentDto.Response toPaymentResponse(Payment payment) {
        return new PaymentDto.Response(
                payment.getId(), payment.getOrderId(), payment.getProductId(), payment.getUserId(),
                payment.getAmount(), payment.getCurrency(), payment.getStatus(),
                payment.getPaymentMethodType(), payment.getTransactionId(), payment.getPaidAt()
        );
    }

    private OrderDto.Response toOrderResponse(Order order) {
        return new OrderDto.Response(
                order.getId(), order.getUserId(), order.getStatus(),
                order.getTotalAmount(), order.getCurrency(),
                order.getCreatedAt(), order.getUpdatedAt()
        );
    }
}
