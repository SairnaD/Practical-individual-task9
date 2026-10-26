package bootcamp.hibernate_practical.exception;

public class BookAlreadyBorrowedException extends RuntimeException {
    public BookAlreadyBorrowedException(Long id) {
        super("Book is already borrowed: " + id);
    }
}
