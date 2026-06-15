package group5.ebay2.image.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import group5.ebay2.image.Image;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
}
