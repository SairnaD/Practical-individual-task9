package bootcamp.hibernate_practical.service;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.entity.Book;
import bootcamp.hibernate_practical.exception.BookAlreadyBorrowedException;
import bootcamp.hibernate_practical.exception.BookNotBorrowedException;
import bootcamp.hibernate_practical.exception.BookNotFoundException;
import bootcamp.hibernate_practical.repository.BookRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getGenre(),
                request.getPublicationYear(),
                true
        );
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        Book book = findBookOrThrow(id);
        return mapToResponse(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, UpdateBookRequest request) {
        Book book = findBookOrThrow(id);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setPublicationYear(request.getPublicationYear());
        return mapToResponse(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long id) {
        bookRepository.delete(findBookOrThrow(id));
    }

    public List<BookResponse> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findByAuthor(String author) {
        return bookRepository.findByAuthorIgnoreCase(author).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findByGenre(String genre) {
        return bookRepository.findByGenreIgnoreCase(genre).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findAvailableBooks(){
        return bookRepository.findByAvailableTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findPublishedYear(int year) {
        return bookRepository.findByPublicationYear(year).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findPublishedAfter(int year) {
        return bookRepository.findByPublicationYearGreaterThan(year).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long countBooks() {
        return bookRepository.count();
    }

    @Transactional
    public BookResponse borrowBook(Long id) {
        Book book = findBookOrThrow(id);
        if (book.isBorrowedStatus()) {
            throw new BookAlreadyBorrowedException(id);
        }
        book.setBorrowedStatus(true);
        book.setAvailable(false);
        return mapToResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse returnBook(Long id) {
        Book book = findBookOrThrow(id);
        if (!book.isBorrowedStatus()) {
            throw new BookNotBorrowedException(id);
        }
        book.setBorrowedStatus(false);
        book.setAvailable(true);
        return mapToResponse(bookRepository.save(book));
    }

    private BookResponse mapToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getPublicationYear(),
                book.isAvailable(),
                book.isBorrowedStatus()
        );
    }
}
