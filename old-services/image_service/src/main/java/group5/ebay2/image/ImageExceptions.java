package group5.ebay2.image;

public class ImageExceptions {

    public static class ImageNotFoundException extends RuntimeException {
        public ImageNotFoundException(String message) {
            super(message);
        }
    }

    public static class ImageStorageException extends RuntimeException {
        public ImageStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
