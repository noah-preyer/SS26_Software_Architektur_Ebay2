package group5.ebay2.product;

import group5.ebay2.product.dtos.BuyProductDto;
import group5.ebay2.product.dtos.BuyProductResponse;
import group5.ebay2.product.dtos.CreateProductDto;
import group5.ebay2.product.dtos.UpdateProductDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts(@RequestParam(required = false) String category) {
        if (category != null) {
            return productService.getProductsByCategory(category);
        }
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(
            @RequestHeader("X-User-Id") UUID sellerId,
            @Valid @RequestBody CreateProductDto dto) {
        return productService.createProduct(dto, sellerId);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID requesterId,
            @Valid @RequestBody UpdateProductDto dto) {
        return productService.updateProduct(id, dto, requesterId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID requesterId) {
        productService.deleteProduct(id, requesterId);
    }

    @PostMapping("/{id}/buy")
    public BuyProductResponse buyProduct(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID buyerId,
            @Valid @RequestBody BuyProductDto dto) {
        return productService.buyProduct(id, buyerId, dto);
    }
}
