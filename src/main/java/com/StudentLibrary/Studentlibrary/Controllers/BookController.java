package com.StudentLibrary.Studentlibrary.Controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.StudentLibrary.Studentlibrary.Model.Author;
import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Model.Genre;
import com.StudentLibrary.Studentlibrary.Services.AuthorService;
import com.StudentLibrary.Studentlibrary.Services.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private AuthorService authorService;

    @PostMapping("/create")
    public ResponseEntity<String> createBook(@RequestBody Book book) {
        bookService.createBook(book);
        return new ResponseEntity<>("Book added to the library system", HttpStatus.CREATED);
    }
    
    @PostMapping(value = "/public/createWithImage", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createBookWithImage(
            @RequestParam("name") String name,
            @RequestParam("genre") String genre,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "publishedYear", required = false) Integer publishedYear,
            @RequestParam("authorName") String authorName,
            @RequestParam("authorEmail") String authorEmail,
            @RequestParam("authorAge") String authorAge,
            @RequestParam("authorCountry") String authorCountry,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Debug logs
            System.out.println("Received book creation request:");
            System.out.println("Name: " + name);
            System.out.println("Genre: " + genre);
            System.out.println("Description: " + description);
            System.out.println("Published Year: " + publishedYear);
            System.out.println("Author Name: " + authorName);
            System.out.println("Author Email: " + authorEmail);
            System.out.println("Author Age: " + authorAge);
            System.out.println("Author Country: " + authorCountry);
            System.out.println("Image present: " + (image != null));
            
            // Parse author age
            int age;
            try {
                age = Integer.parseInt(authorAge);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid author age format"));
            }
            
            // Create or find author
            Author author = new Author(authorName, authorEmail, age, authorCountry);
            author = authorService.createOrUpdateAuthor(author);
            
            // Create book
            Book book = new Book();
            book.setName(name);
            book.setDescription(description);
            book.setPublishedYear(publishedYear);
            try {
                // Try to match the genre case-insensitively
                Genre matchedGenre = null;
                for (Genre g : Genre.values()) {
                    if (g.name().equalsIgnoreCase(genre)) {
                        matchedGenre = g;
                        break;
                    }
                }
                
                if (matchedGenre == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "Invalid genre: " + genre + ". Valid genres are: " + Arrays.toString(Genre.values())
                    ));
                }
                
                book.setGenre(matchedGenre);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid genre: " + genre + ". Valid genres are: " + Arrays.toString(Genre.values())
                ));
            }
            book.setAuthor(author);
            book.setAvailable(true);
            
            // Save image if provided
            if (image != null && !image.isEmpty()) {
                book = bookService.createBookWithImage(book, image);
            } else {
                book = bookService.createBook(book);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book added successfully");
            response.put("id", book.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to create book: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> getBooks(
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "available", required = false, defaultValue = "false") boolean available,
            @RequestParam(value = "author", required = false) String author) {
        List<Book> bookList = bookService.getBooks(genre, available, author);
        return new ResponseEntity<>(bookList, HttpStatus.OK);
    }
    
    @GetMapping("/public/all")
    public ResponseEntity<?> getAllBooks() {
        try {
            System.out.println("Controller: Fetching all books");
            List<Book> books = bookService.getAllBooks();
            System.out.println("Controller: Found " + books.size() + " books");
            
            // Add more detailed logging
            for (Book book : books) {
                System.out.println("Book ID: " + book.getId() + 
                                  ", Name: " + book.getName() + 
                                  ", Genre: " + book.getGenre() +
                                  ", Author: " + (book.getAuthor() != null ? book.getAuthor().getName() : "null") +
                                  ", Available: " + book.isAvailable());
            }
            
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("Controller: Error fetching books: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch books: " + e.getMessage()));
        }
    }
    
    @GetMapping("/public/findById")
    public ResponseEntity<Book> getBookById(@RequestParam int id) {
        try {
            Book book = bookService.getBookById(id);
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/public/image")
    public ResponseEntity<?> getBookImage(@RequestParam int id) {
        try {
            byte[] imageData = bookService.getBookImage(id);
            if (imageData != null && imageData.length > 0) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageData);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to retrieve image: " + e.getMessage()));
        }
    }
    
    @PutMapping("/public/updateBook")
    public ResponseEntity<?> updateBook(@RequestBody Book book) {
        try {
            System.out.println("Controller: Updating book with ID: " + book.getId());
            System.out.println("Controller: Book data: " + book);
            System.out.println("Controller: Author ID: " + (book.getAuthor() != null ? book.getAuthor().getId() : "null"));
            
            Book updatedBook = bookService.updateBook(book);
            System.out.println("Controller: Book updated successfully: " + updatedBook);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            System.err.println("Controller: Error updating book: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update book: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/public/deleteBook")
    public ResponseEntity<?> deleteBook(@RequestParam int id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete book: " + e.getMessage()));
        }
    }
}
