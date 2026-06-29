package group5.ebay2.order;

import group5.ebay2.order.dtos.OrderDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order")
    public ResponseEntity<OrderDto.Response> createOrder(
            @Valid @RequestBody OrderDto.CreateRequest request) {
        OrderDto.Response response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderDto.Response> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<OrderDto.Response> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderDto.StatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

    @GetMapping("/order/user/{userId}")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PutMapping("/order/{orderId}/paid")
    public ResponseEntity<OrderDto.Response> markOrderPaid(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.markOrderPaid(orderId));
    }

    @PutMapping("/order/{orderId}/refunded")
    public ResponseEntity<OrderDto.Response> markOrderRefunded(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.markOrderRefunded(orderId));
    }

    @PostMapping("/order/{orderId}/refund")
    public ResponseEntity<OrderDto.Response> refundOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.refundOrder(orderId));
    }
}
