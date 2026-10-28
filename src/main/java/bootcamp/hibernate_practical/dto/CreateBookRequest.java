package bootcamp.hibernate_practical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookRequest {
    @NotBlank(message = "Title must not be empty")
    private String title;
    @NotBlank(message = "Author must not be empty")
    private String author;
    @NotBlank(message = "Genre must not be empty")
    private String genre;
    @Positive(message = "Publication year must be positive number")
    private int publicationYear;
}
