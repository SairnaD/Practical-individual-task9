package bootcamp.hibernate_practical.exception;

public class BookNotBorrowedException extends RuntimeException {
    public BookNotBorrowedException(Long id) {
        super("Book is not borrowed: " + id);
    }
}
