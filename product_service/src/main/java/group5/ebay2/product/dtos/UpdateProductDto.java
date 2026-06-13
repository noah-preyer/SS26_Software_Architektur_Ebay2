package group5.ebay2.product.dtos;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class UpdateProductDto {

    private String title;
    private String description;

    @Positive
    private BigDecimal price;

    private String category;
    private List<String> imageUrls;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
