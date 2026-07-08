package group5.ebay2.order;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 255)
    private String productTitle;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    protected OrderItem() {}

    public OrderItem(Long productId, String productTitle, Integer quantity, BigDecimal price, Order order) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.quantity = quantity;
        this.price = price;
        this.order = order;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductTitle() { return productTitle; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotal() { return price.multiply(BigDecimal.valueOf(quantity)); }
    public Order getOrder() { return order; }
    void setOrder(Order order) { this.order = order; }
}
