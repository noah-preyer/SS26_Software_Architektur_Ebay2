package group5.ebay2.product;

import group5.ebay2.product.dtos.BuyProductDto;
import group5.ebay2.product.dtos.BuyProductResponse;
import group5.ebay2.product.dtos.CreateProductDto;
import group5.ebay2.product.dtos.UpdateProductDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final PaymentClient paymentClient;

    public ProductService(ProductRepository productRepository, PaymentClient paymentClient) {
        this.productRepository = productRepository;
        this.paymentClient = paymentClient;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product createProduct(CreateProductDto dto, UUID sellerId) {
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

    public Product updateProduct(UUID id, UpdateProductDto dto, UUID requesterId) {
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

    public void deleteProduct(UUID id, UUID requesterId) {
        Product product = getProductById(id);
        if (!product.getSellerId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the seller of this product");
        }
        productRepository.delete(product);
    }

    public BuyProductResponse buyProduct(UUID productId, UUID buyerId, BuyProductDto dto) {
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
            PaymentClient.OrderResponse order = paymentClient.createOrder(
                    buyerId, productId, product.getPrice(), dto.getCurrency());

            PaymentClient.PaymentResponse payment = paymentClient.processPayment(
                    order.id(), productId, buyerId,
                    product.getPrice(), dto.getCurrency(), dto.getPaymentMethodType());

            return new BuyProductResponse(
                    product.getId(), product.getTitle(), product.getPrice(),
                    product.getQuantity(), order.id(), order.status(),
                    payment.id(), payment.status(), payment.transactionId(), payment.paidAt());

        } catch (Exception e) {
            // Compensating transaction: restore stock on payment failure
            productRepository.incrementQuantity(productId);
            if (product.getStatus() == ProductStatus.SOLD) {
                product.setStatus(ProductStatus.AVAILABLE);
                productRepository.save(product);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment failed, purchase rolled back: " + e.getMessage());
        }
    }
}
