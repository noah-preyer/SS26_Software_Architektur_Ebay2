package group5.ebay2.image;

import group5.ebay2.image.dtos.ImageDto;
import group5.ebay2.image.repositories.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {
    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;
    private final Path storagePath;

    public ImageService(ImageRepository imageRepository,
                        @Value("${image.storage.path}") String storagePath) {
        this.imageRepository = imageRepository;
        this.storagePath = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storagePath);
        } catch (IOException e) {
            throw new ImageExceptions.ImageStorageException("Could not create storage directory", e);
        }
    }

    @Transactional
    public ImageDto.UploadResponse uploadImage(UUID userId, MultipartFile file, String objectKey) {
        log.info("Uploading image for user: {}, original: {}", userId, file.getOriginalFilename());

        String storedFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = this.storagePath.resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ImageExceptions.ImageStorageException("Failed to store file: " + storedFilename, e);
        }

        Image image = new Image(
                userId,
                file.getOriginalFilename(),
                storedFilename,
                targetPath.toString(),
                file.getContentType(),
                file.getSize(),
                objectKey
        );

        Image saved = imageRepository.save(image);
        log.info("Image stored id: {} path: {}", saved.getId(), saved.getStoredFilename());

        return toUploadResponse(saved);
    }

    @Transactional(readOnly = true)
    public ImageDto.Response getMeta(UUID id) {
        return toResponse(findImage(id));
    }

    @Transactional(readOnly = true)
    public Resource serveImage(UUID id) {
        Image image = findImage(id);
        try {
            Path filePath = Paths.get(image.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new ImageExceptions.ImageNotFoundException("File not readable: " + image.getStoredFilename());
        } catch (MalformedURLException e) {
            throw new ImageExceptions.ImageStorageException("Invalid file path", e);
        }
    }

    @Transactional
    public void deleteImage(UUID id) {
        Image image = findImage(id);
        try {
            Files.deleteIfExists(Paths.get(image.getFilePath()));
        } catch (IOException e) {
            log.warn("Could not delete file on disk: {}", image.getFilePath());
        }
        imageRepository.delete(image);
        log.info("Deleted image id: {}", id);
    }

    private Image findImage(UUID id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new ImageExceptions.ImageNotFoundException("Image not found: " + id));
    }

    private ImageDto.UploadResponse toUploadResponse(Image image) {
        return new ImageDto.UploadResponse(
                image.getId(), image.getOriginalFilename(),
                image.getContentType(), image.getFileSize(),
                image.getObjectKey(), image.getCreatedAt()
        );
    }

    private ImageDto.Response toResponse(Image image) {
        return new ImageDto.Response(
                image.getId(), image.getUserId(),
                image.getOriginalFilename(), image.getStoredFilename(),
                image.getContentType(), image.getFileSize(),
                image.getObjectKey(), image.getCreatedAt()
        );
    }
}
