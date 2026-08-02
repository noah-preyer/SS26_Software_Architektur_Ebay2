package group5.ebay2.order;

import group5.ebay2.order.dtos.OrderDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto.Response> createOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long xUserId,
            @Valid @RequestBody OrderDto.CreateRequest request) {
        Long userId = xUserId != null ? xUserId : request.userId();
        OrderDto.Response response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/checkout")
    public ResponseEntity<List<OrderDto.CheckoutItemResult>> checkout(
            @RequestHeader(value = "X-User-Id", required = false) Long xUserId,
            @Valid @RequestBody OrderDto.CheckoutRequest request) {
        Long userId = xUserId != null ? xUserId : request.userId();
        List<OrderDto.CheckoutItemResult> results = orderService.checkout(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @PutMapping("/{orderId}/paid")
    public ResponseEntity<OrderDto.Response> markOrderPaid(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.markOrderPaid(orderId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto.Response> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }
}
