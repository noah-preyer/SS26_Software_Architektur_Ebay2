package group5.ebay2.payment;

public class PaymentExceptions {

    public static class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(String message) {
            super(message);
        }
    }

    public static class PaymentAlreadyRefundedException extends RuntimeException {
        public PaymentAlreadyRefundedException(String message) {
            super(message);
        }
    }

    public static class InvalidPaymentStateException extends RuntimeException {
        public InvalidPaymentStateException(String message) {
            super(message);
        }
    }

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidOrderStateException extends RuntimeException {
        public InvalidOrderStateException(String message) {
            super(message);
        }
    }
}
