package group5.ebay2.order;

public class OrderExceptions {

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }

    public static class OrderAlreadyExistsException extends RuntimeException {
        public OrderAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class InvalidOrderStateException extends RuntimeException {
        public InvalidOrderStateException(String message) {
            super(message);
        }
    }
}
