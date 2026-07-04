package group5.ebay2.product;

import group5.ebay2.product.dtos.BuyProductResponse;
import group5.ebay2.product.dtos.CreateProductDto;
import group5.ebay2.product.dtos.ProductResponse;
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
    private final UserClient userClient;

    public ProductService(ProductRepository productRepository, OrderClient orderClient, UserClient userClient) {
        this.productRepository = productRepository;
        this.orderClient = orderClient;
        this.userClient = userClient;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findByStatus(ProductStatus.AVAILABLE).stream()
                .map(p -> ProductResponse.from(p, null))
                .toList();
    }

    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndStatus(category, ProductStatus.AVAILABLE).stream()
                .map(p -> ProductResponse.from(p, null))
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        String sellerUsername = userClient.getUsernameById(product.getSellerId());
        return ProductResponse.from(product, sellerUsername);
    }

    private Product getProductEntityById(Long id) {
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
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, UpdateProductDto dto, Long requesterId) {
        Product product = getProductEntityById(id);
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
        Product product = getProductEntityById(id);
        if (!product.getSellerId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the seller of this product");
        }
        productRepository.delete(product);
    }

    public BuyProductResponse buyProduct(Long productId, Long buyerId) {
        Product product = getProductEntityById(productId);

        if (product.getSellerId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot buy your own product");
        }

        int updated = productRepository.markAsSold(productId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is already sold");
        }

        try {
            OrderClient.OrderResponse order = orderClient.createOrder(buyerId, productId, DEFAULT_CURRENCY);
            OrderClient.OrderResponse paidOrder = orderClient.markOrderPaid(order.id());

            return new BuyProductResponse(
                    product.getId(), product.getTitle(), product.getPrice(),
                    paidOrder.id(), paidOrder.status());

        } catch (Exception e) {
            // Compensating transaction: restore availability if order creation fails
            productRepository.markAsAvailable(productId);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Order creation failed, purchase rolled back: " + e.getMessage());
        }
    }
}
