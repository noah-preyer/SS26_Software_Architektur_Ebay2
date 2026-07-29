package group5.ebay2.product;

import group5.ebay2.product.dtos.CreateProductDto;
import group5.ebay2.product.dtos.ProductResponse;
import group5.ebay2.product.dtos.UpdateProductDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserClient userClient;

    public ProductService(ProductRepository productRepository, UserClient userClient) {
        this.productRepository = productRepository;
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

    private static final String DEFAULT_IMAGE_URL = "https://brunofuga.adv.br/?s=no-alcohol-icon-png-and-svg-vector-free-download-cc-v361DOCK";

    public Product createProduct(CreateProductDto dto, Long sellerId) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setSellerId(sellerId);
        List<String> images = dto.getImageUrls();
        if (images == null || images.isEmpty()) {
            images = List.of(DEFAULT_IMAGE_URL);
        }
        product.setImageUrls(images);
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

    public int reserveProduct(Long id) {
        return productRepository.markAsSold(id);
    }

    public boolean unreserveProduct(Long id) {
        return productRepository.findById(id).map(p -> {
            productRepository.markAsAvailable(id);
            return true;
        }).orElse(false);
    }
}
