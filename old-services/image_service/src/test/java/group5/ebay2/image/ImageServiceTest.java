package group5.ebay2.image;

import group5.ebay2.image.dtos.ImageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Test
    void uploadImage_shouldStoreFileAndReturnMeta() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", MediaType.IMAGE_PNG_VALUE, "fake-image-content".getBytes());

        ImageDto.UploadResponse response = imageService.uploadImage(UUID.randomUUID(), file, "users/1/profile.png");

        assertThat(response.id()).isNotNull();
        assertThat(response.originalFilename()).isEqualTo("test.png");
        assertThat(response.contentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);
        assertThat(response.fileSize()).isEqualTo("fake-image-content".getBytes().length);
        assertThat(response.objectKey()).isEqualTo("users/1/profile.png");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void getMeta_shouldReturnImageMeta() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "jpg-data".getBytes());
        ImageDto.UploadResponse uploaded = imageService.uploadImage(UUID.randomUUID(), file, null);

        ImageDto.Response meta = imageService.getMeta(uploaded.id());

        assertThat(meta.id()).isEqualTo(uploaded.id());
        assertThat(meta.originalFilename()).isEqualTo("photo.jpg");
        assertThat(meta.storedFilename()).isNotNull();
    }

    @Test
    void getMeta_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> imageService.getMeta(UUID.randomUUID()))
                .isInstanceOf(ImageExceptions.ImageNotFoundException.class);
    }

    @Test
    void serveImage_shouldReturnResource() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", MediaType.IMAGE_PNG_VALUE, "image-bytes".getBytes());
        ImageDto.UploadResponse uploaded = imageService.uploadImage(UUID.randomUUID(), file, null);

        var resource = imageService.serveImage(uploaded.id());

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void deleteImage_shouldRemoveMetaAndFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "delete-me.jpg", MediaType.IMAGE_JPEG_VALUE, "to-delete".getBytes());
        ImageDto.UploadResponse uploaded = imageService.uploadImage(UUID.randomUUID(), file, null);

        imageService.deleteImage(uploaded.id());

        assertThatThrownBy(() -> imageService.getMeta(uploaded.id()))
                .isInstanceOf(ImageExceptions.ImageNotFoundException.class);
    }

    @Test
    void uploadImage_withoutObjectKey_shouldSucceed() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "no-key.gif", MediaType.IMAGE_GIF_VALUE, "gif-data".getBytes());

        ImageDto.UploadResponse response = imageService.uploadImage(UUID.randomUUID(), file, null);

        assertThat(response.id()).isNotNull();
        assertThat(response.objectKey()).isNull();
    }
}
