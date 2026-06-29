package group5.ebay2.product;

import group5.ebay2.product.dtos.BuyProductResponse;
import group5.ebay2.product.dtos.CreateProductDto;
import group5.ebay2.product.dtos.UpdateProductDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private static final String DEFAULT_CURRENCY = "EUR";

    private final ProductRepository productRepository;
    private final OrderClient orderClient;

    public ProductService(ProductRepository productRepository, OrderClient orderClient) {
        this.productRepository = productRepository;
        this.orderClient = orderClient;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product createProduct(CreateProductDto dto, Long sellerId) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setSellerId(sellerId);
        product.setImageUrls(dto.getImageUrls());
        product.setQuantity(dto.getQuantity());
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, UpdateProductDto dto, Long requesterId) {
        Product product = getProductById(id);
        if (!product.getSellerId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the seller of this product");
        }
        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getImageUrls() != null) product.setImageUrls(dto.getImageUrls());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id, Long requesterId) {
        Product product = getProductById(id);
        if (!product.getSellerId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the seller of this product");
        }
        productRepository.delete(product);
    }

    public BuyProductResponse buyProduct(Long productId, Long buyerId) {
        Product product = getProductById(productId);

        if (product.getSellerId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot buy your own product");
        }

        int updated = productRepository.decrementQuantity(productId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is out of stock");
        }

        // Reload to get current quantity after decrement
        product = getProductById(productId);
        if (product.getQuantity() == 0) {
            product.setStatus(ProductStatus.SOLD);
            productRepository.save(product);
        }

        try {
            OrderClient.OrderResponse order = orderClient.createOrder(buyerId, productId, DEFAULT_CURRENCY);
            // Kein separater Payment-Service mehr vorhanden: Kauf gilt als sofort bezahlt,
            // der order_service-eigene Scheduler übernimmt danach PAID -> SHIPPED -> DELIVERED.
            OrderClient.OrderResponse paidOrder = orderClient.markOrderPaid(order.id());

            return new BuyProductResponse(
                    product.getId(), product.getTitle(), product.getPrice(), product.getQuantity(),
                    paidOrder.id(), paidOrder.status());

        } catch (Exception e) {
            // Compensating transaction: restore stock if order creation/payment fails
            productRepository.incrementQuantity(productId);
            if (product.getStatus() == ProductStatus.SOLD) {
                product.setStatus(ProductStatus.AVAILABLE);
                productRepository.save(product);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Order creation failed, purchase rolled back: " + e.getMessage());
        }
    }
}
