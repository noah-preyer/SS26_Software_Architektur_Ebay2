package group5.ebay2.product;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(List.of(
            product("iPhone 14 Pro 256GB Space Black", "Kaum genutzt, keine Kratzer, Originalzubehör vorhanden.", new BigDecimal("749.99"), "Elektronik", UUID.fromString("00000000-0000-0000-0000-000000000001")),
            product("Sony WH-1000XM5 Kopfhörer", "Noise-Cancelling Bluetooth Kopfhörer, sehr guter Zustand.", new BigDecimal("249.00"), "Elektronik", UUID.fromString("00000000-0000-0000-0000-000000000001")),
            product("Nike Air Max 90 Gr. 43", "Sneaker in Größe 43, kaum getragen, weiß/schwarz.", new BigDecimal("89.50"), "Schuhe", UUID.fromString("00000000-0000-0000-0000-000000000002")),
            product("LEGO Technic Bugatti Chiron 42083", "Vollständig und ungeöffnet, OVP.", new BigDecimal("319.00"), "Spielzeug", UUID.fromString("00000000-0000-0000-0000-000000000002")),
            product("Dyson V11 Absolute Staubsauger", "Akkustaubsauger mit allen Aufsätzen, top Zustand.", new BigDecimal("399.00"), "Haushalt", UUID.fromString("00000000-0000-0000-0000-000000000003")),
            product("MacBook Pro M2 14\" 512GB", "2023er Modell, Space Grau, Akku 95% Kapazität.", new BigDecimal("1499.00"), "Elektronik", UUID.fromString("00000000-0000-0000-0000-000000000003")),
            product("Harry Potter Buchset 1-7", "Alle 7 Bände der deutschen Ausgabe, guter Zustand.", new BigDecimal("45.00"), "Bücher", UUID.fromString("00000000-0000-0000-0000-000000000004")),
            product("Garmin Forerunner 255 Smartwatch", "GPS-Laufuhr, schwarz, inkl. Ladekabel.", new BigDecimal("199.00"), "Sport", UUID.fromString("00000000-0000-0000-0000-000000000004"))
        ));
    }

    private Product product(String title, String description, BigDecimal price, String category, UUID sellerId) {
        Product p = new Product();
        p.setTitle(title);
        p.setDescription(description);
        p.setPrice(price);
        p.setCategory(category);
        p.setSellerId(sellerId);
        p.setQuantity(5);
        p.setStatus(ProductStatus.AVAILABLE);
        return p;
    }
}