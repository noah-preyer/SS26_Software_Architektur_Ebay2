package group5.ebay2.order;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 255)
    private String productTitle;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    public Order(UUID userId, UUID productId, String productTitle, BigDecimal totalAmount, String currency) {
        this.userId = userId;
        this.productId = productId;
        this.productTitle = productTitle;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = "CREATED";
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markPaid() {
        this.status = "PAID";
    }

    public void markShipped() {
        this.status = "SHIPPED";
    }

    public void markDelivered() {
        this.status = "DELIVERED";
    }

    public void markRefunded() {
        this.status = "REFUNDED";
    }

    public void markCancelled() {
        this.status = "CANCELLED";
    }
}
