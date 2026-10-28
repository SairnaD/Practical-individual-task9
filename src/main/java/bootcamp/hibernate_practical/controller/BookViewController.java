package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.service.BookService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class BookViewController {
    private final BookService bookService;

    public BookViewController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String list(Model model, Authentication auth) {
        fillModel(model, auth);
        model.addAttribute("bookForm", new CreateBookRequest());
        return "books";
    }

    @PostMapping("/books")
    public String create(@Valid @ModelAttribute("bookForm") CreateBookRequest form,
                         BindingResult result, Model model, Authentication auth) {
        if (result.hasErrors()) {
            fillModel(model, auth);
            return "books";
        }
        bookService.createBook(form);
        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }

    private void fillModel(Model model, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("username", auth.getName());
        model.addAttribute("isAdmin", isAdmin);
    }
}