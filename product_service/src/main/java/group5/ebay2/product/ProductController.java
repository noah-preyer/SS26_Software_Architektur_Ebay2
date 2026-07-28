package group5.ebay2.product;

import group5.ebay2.product.dtos.CreateProductDto;
import group5.ebay2.product.dtos.ProductResponse;
import group5.ebay2.product.dtos.UpdateProductDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts(@RequestParam(required = false) String category) {
        if (category != null) {
            return productService.getProductsByCategory(category);
        }
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(
            @RequestHeader("X-User-Id") Long sellerId,
            @Valid @RequestBody CreateProductDto dto) {
        return productService.createProduct(dto, sellerId);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @Valid @RequestBody UpdateProductDto dto) {
        return productService.updateProduct(id, dto, requesterId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId) {
        productService.deleteProduct(id, requesterId);
    }

    @PutMapping("/{id}/reserve")
    @ResponseStatus(HttpStatus.OK)
    public void reserveProduct(@PathVariable Long id) {
        int updated = productService.reserveProduct(id);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is already sold or not found");
        }
    }

    @PutMapping("/{id}/unreserve")
    @ResponseStatus(HttpStatus.OK)
    public void unreserveProduct(@PathVariable Long id) {
        boolean found = productService.unreserveProduct(id);
        if (!found) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }
}
