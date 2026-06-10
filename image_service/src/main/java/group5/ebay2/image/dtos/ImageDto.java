package group5.ebay2.image.dtos;

import java.time.Instant;
import java.util.UUID;

public class ImageDto {

    public record UploadResponse(
            UUID id,
            String originalFilename,
            String contentType,
            Long fileSize,
            String objectKey,
            Instant createdAt
    ) {
    }

    public record Response(
            UUID id,
            UUID userId,
            String originalFilename,
            String storedFilename,
            String contentType,
            Long fileSize,
            String objectKey,
            Instant createdAt
    ) {
    }
}
