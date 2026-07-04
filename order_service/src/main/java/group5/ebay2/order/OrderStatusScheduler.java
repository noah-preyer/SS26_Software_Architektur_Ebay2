package group5.ebay2.order;

import group5.ebay2.order.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class OrderStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    private static final long STATUS_DELAY_SECONDS = 10;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public OrderStatusScheduler(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 1000)
    public void updateOrderStatuses() {
        Instant threshold = Instant.now().minus(STATUS_DELAY_SECONDS, ChronoUnit.SECONDS);

        updatePaidToShipped(threshold);
        updateShippedToDelivered(threshold);
    }

    private void updatePaidToShipped(Instant threshold) {
        List<Order> paidOrders = orderRepository.findByStatusAndUpdatedAtBefore("PAID", threshold);

        for (Order order : paidOrders) {
            log.info("Auto-updating order {} from PAID to SHIPPED", order.getId());
            orderService.markOrderShipped(order.getId());
        }

        if (!paidOrders.isEmpty()) {
            log.info("Auto-updated {} orders from PAID to SHIPPED", paidOrders.size());
        }
    }

    private void updateShippedToDelivered(Instant threshold) {
        List<Order> shippedOrders = orderRepository.findByStatusAndUpdatedAtBefore("SHIPPED", threshold);

        for (Order order : shippedOrders) {
            log.info("Auto-updating order {} from SHIPPED to DELIVERED", order.getId());
            orderService.markOrderDelivered(order.getId());
        }

        if (!shippedOrders.isEmpty()) {
            log.info("Auto-updated {} orders from SHIPPED to DELIVERED", shippedOrders.size());
        }
    }
}
