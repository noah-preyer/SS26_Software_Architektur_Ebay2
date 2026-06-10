package group5.ebay2.user;

public class UserExceptions {

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class AddressNotFoundException extends RuntimeException {
        public AddressNotFoundException(String message) {
            super(message);
        }
    }

    public static class AddressTypeNotFoundException extends RuntimeException {
        public AddressTypeNotFoundException(String message) {
            super(message);
        }
    }
}