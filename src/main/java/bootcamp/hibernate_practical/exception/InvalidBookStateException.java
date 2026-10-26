package bootcamp.hibernate_practical.exception;

public class InvalidBookStateException extends RuntimeException {
    public InvalidBookStateException(String message) {
        super(message);
    }
}
