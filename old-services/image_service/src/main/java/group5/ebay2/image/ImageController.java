package group5.ebay2.image;

import group5.ebay2.image.dtos.ImageDto;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/image/upload")
    public ResponseEntity<ImageDto.UploadResponse> uploadImage(
            @RequestParam("userId") UUID userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "objectKey", required = false) String objectKey) {
        ImageDto.UploadResponse response = imageService.uploadImage(userId, file, objectKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<Resource> serveImage(@PathVariable UUID id) {
        Resource resource = imageService.serveImage(id);
        ImageDto.Response meta = imageService.getMeta(id);
        String contentType = meta.contentType() != null ? meta.contentType() : "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + meta.originalFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/image/{id}/meta")
    public ResponseEntity<ImageDto.Response> getMeta(@PathVariable UUID id) {
        return ResponseEntity.ok(imageService.getMeta(id));
    }

    @DeleteMapping("/image/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
