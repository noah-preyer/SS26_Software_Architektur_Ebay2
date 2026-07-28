package group5.ebay2.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Order() {}

    public Order(Long userId, String currency) {
        this.userId = userId;
        this.currency = currency;
        this.totalAmount = BigDecimal.ZERO;
        this.status = "CREATED";
    }

    @PrePersist
    protected void onCreate() { Instant now = Instant.now(); this.createdAt = now; this.updatedAt = now; }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        totalAmount = totalAmount.add(item.getTotal());
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public List<OrderItem> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void markPaid() { this.status = "PAID"; }
    public void markShipped() { this.status = "SHIPPED"; }
    public void markDelivered() { this.status = "DELIVERED"; }
    public void markRefunded() { this.status = "REFUNDED"; }
}
