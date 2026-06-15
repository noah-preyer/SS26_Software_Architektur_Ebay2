package group5.ebay2.payment;

import group5.ebay2.payment.dtos.PaymentDto;
import group5.ebay2.payment.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderServiceClient orderServiceClient) {
        this.paymentRepository = paymentRepository;
        this.orderServiceClient = orderServiceClient;
    }

    @Transactional
    public PaymentDto.Response processPayment(PaymentDto.ProcessRequest request) {
        log.info("Processing payment for order: {} amount: {} {}", request.orderId(), request.amount(), request.currency());

        OrderDto order = orderServiceClient.getOrder(request.orderId());

        if (!"CREATED".equals(order.status())) {
            throw new PaymentExceptions.InvalidOrderStateException(
                    "Cannot pay for order with status: " + order.status());
        }

        Payment payment = new Payment(
                request.orderId(), request.productId(), request.userId(),
                request.amount(), request.currency(), request.paymentMethodType()
        );
        payment.markCompleted("txn_" + UUID.randomUUID().toString().replace("-", ""));

        paymentRepository.save(payment);

        orderServiceClient.markOrderPaid(request.orderId());

        log.info("Payment completed id: {} for order: {}", payment.getId(), request.orderId());

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

        payment.markRefunded();
        paymentRepository.save(payment);

        orderServiceClient.markOrderRefunded(payment.getOrderId());

        String reason = request != null ? request.reason() : null;
        log.info("Refunded payment id: {} order: {} reason: {}", id, payment.getOrderId(), reason);

        return toPaymentResponse(payment);
    }

    @Transactional
    public PaymentDto.Response refundPaymentByOrderId(UUID orderId) {
        log.info("Refunding payment for order: {}", orderId);

        List<Payment> payments = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        
        Payment payment = payments.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .findFirst()
                .orElseThrow(() -> new PaymentExceptions.PaymentNotFoundException(
                        "No completed payment found for order: " + orderId));

        payment.markRefunded();
        paymentRepository.save(payment);

        log.info("Refunded payment id: {} for order: {}", payment.getId(), orderId);

        return toPaymentResponse(payment);
    }

    private PaymentDto.Response toPaymentResponse(Payment payment) {
        return new PaymentDto.Response(
                payment.getId(), payment.getOrderId(), payment.getProductId(), payment.getUserId(),
                payment.getAmount(), payment.getCurrency(), payment.getStatus(),
                payment.getPaymentMethodType(), payment.getTransactionId(), payment.getPaidAt()
        );
    }
}
