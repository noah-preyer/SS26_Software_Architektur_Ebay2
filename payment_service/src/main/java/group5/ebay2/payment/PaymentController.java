package group5.ebay2.payment;

import group5.ebay2.payment.dtos.OrderDto;
import group5.ebay2.payment.dtos.PaymentDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment/process")
    public ResponseEntity<PaymentDto.Response> processPayment(
            @Valid @RequestBody PaymentDto.ProcessRequest request) {
        PaymentDto.Response response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/payment/{id}")
    public ResponseEntity<PaymentDto.Response> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PostMapping("/payment/{id}/refund")
    public ResponseEntity<PaymentDto.Response> refundPayment(
            @PathVariable UUID id,
            @RequestBody(required = false) PaymentDto.RefundRequest request) {
        return ResponseEntity.ok(paymentService.refundPayment(id, request));
    }

    @GetMapping("/payment/order/{orderId}")
    public ResponseEntity<List<PaymentDto.Response>> getPaymentsByOrder(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }

    @PostMapping("/order")
    public ResponseEntity<OrderDto.Response> createOrder(
            @Valid @RequestBody OrderDto.CreateRequest request) {
        OrderDto.Response response = paymentService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderDto.Response> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getOrder(orderId));
    }

    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<OrderDto.Response> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderDto.StatusUpdateRequest request) {
        return ResponseEntity.ok(paymentService.updateOrderStatus(orderId, request));
    }

    @GetMapping("/order/user/{userId}")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(paymentService.getOrdersByUser(userId));
    }
}
