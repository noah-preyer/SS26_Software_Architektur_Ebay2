package group5.ebay2.product;

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

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

    public Product createProduct(CreateProductDto dto) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setSellerId(dto.getSellerId());
        product.setImageUrls(dto.getImageUrls());
        return productRepository.save(product);
    }

    public Product updateProduct(UUID id, UpdateProductDto dto) {
        Product product = getProductById(id);
        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getImageUrls() != null) product.setImageUrls(dto.getImageUrls());
        return productRepository.save(product);
    }

    public void deleteProduct(UUID id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public Product buyProduct(UUID id) {
        Product product = getProductById(id);
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is already sold");
        }
        product.setStatus(ProductStatus.SOLD);
        return productRepository.save(product);
    }
}
