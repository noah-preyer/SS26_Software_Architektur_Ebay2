package group5.ebay2.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, length = 100)
    private String productId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 30)
    private String paymentMethodType;

    private String transactionId;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant paidAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Payment() {
    }

    public Payment(UUID orderId, String productId, UUID userId, BigDecimal amount, String currency, String paymentMethodType) {
        this.orderId = orderId;
        this.productId = productId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethodType = paymentMethodType;
        this.status = "PENDING";
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

    public UUID getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethodType() {
        return paymentMethodType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void markCompleted(String transactionId) {
        this.status = "COMPLETED";
        this.transactionId = transactionId;
        this.paidAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }

    public void markRefunded() {
        this.status = "REFUNDED";
    }
}
