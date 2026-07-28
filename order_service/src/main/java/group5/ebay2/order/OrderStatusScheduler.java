package group5.ebay2.order;

import group5.ebay2.order.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OrderStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    private static final long TRANSITION_DELAY_SECONDS = 10;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public OrderStatusScheduler(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 10000)
    public void processOrderStatusTransitions() {
        Instant cutoff = Instant.now().minusSeconds(TRANSITION_DELAY_SECONDS);

        List<Order> createdOrders = orderRepository.findByStatusAndUpdatedAtBefore("CREATED", cutoff);
        for (Order order : createdOrders) {
            try {
                orderService.markOrderPaid(order.getId());
                log.info("Scheduler: Order {} transitioned CREATED -> PAID", order.getId());
            } catch (Exception e) {
                log.error("Scheduler: Failed to transition order {}: {}", order.getId(), e.getMessage());
            }
        }

        List<Order> paidOrders = orderRepository.findByStatusAndUpdatedAtBefore("PAID", cutoff);
        for (Order order : paidOrders) {
            try {
                order.markShipped();
                orderRepository.save(order);
                log.info("Scheduler: Order {} transitioned PAID -> SHIPPED", order.getId());
            } catch (Exception e) {
                log.error("Scheduler: Failed to ship order {}: {}", order.getId(), e.getMessage());
            }
        }

        List<Order> shippedOrders = orderRepository.findByStatusAndUpdatedAtBefore("SHIPPED", cutoff);
        for (Order order : shippedOrders) {
            try {
                order.markDelivered();
                orderRepository.save(order);
                log.info("Scheduler: Order {} transitioned SHIPPED -> DELIVERED", order.getId());
            } catch (Exception e) {
                log.error("Scheduler: Failed to deliver order {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}