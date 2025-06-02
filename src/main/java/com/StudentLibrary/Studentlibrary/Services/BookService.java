package com.StudentLibrary.Studentlibrary.Services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Repositories.BookRepository;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Book createBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        return bookRepository.save(book);
    }
    
    public Book createBookWithImage(Book book, MultipartFile image) throws IOException {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        
        if (image != null && !image.isEmpty()) {
            book.setCoverImage(image.getBytes());
        }
        
        return bookRepository.save(book);
    }

    public List<Book> getBooks(String genre, boolean isAvailable, String author) {
        if (genre != null && author != null) {
            return bookRepository.findBooksByGenre_Author(genre, author, isAvailable);
        } else if (genre != null) {
            return bookRepository.findBooksByGenre(genre, isAvailable);
        } else if (author != null) {
            return bookRepository.findBooksByAuthor(author, isAvailable);
        }
        return bookRepository.findBooksByAvailability(isAvailable);
    }
    
    public List<Book> getAllBooks() {
        try {
            System.out.println("Service: Fetching all books");
            List<Book> books = bookRepository.findAll();
            System.out.println("Service: Found " + books.size() + " books");
            return books;
        } catch (Exception e) {
            System.err.println("Service: Error fetching books: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public Book getBookById(int id) {
        try {
            System.out.println("Service: Fetching book with id: " + id);
            return bookRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        } catch (Exception e) {
            System.err.println("Service: Error fetching book by id: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public byte[] getBookImage(int id) {
        try {
            Book book = getBookById(id);
            return book.getCoverImage();
        } catch (Exception e) {
            System.err.println("Service: Error fetching book image: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public Book updateBook(Book book) {
        try {
            System.out.println("Service: Updating book with ID: " + book.getId());
            System.out.println("Service: Book data: " + book);
            
            // Check if book exists
            if (!bookRepository.existsById(book.getId())) {
                throw new RuntimeException("Book not found with id: " + book.getId());
            }
            
            // Update the book
            int updated = bookRepository.updateBook(book);
            System.out.println("Service: Update result: " + updated + " rows affected");
            
            // Return the updated book
            Book updatedBook = getBookById(book.getId());
            System.out.println("Service: Updated book: " + updatedBook);
            return updatedBook;
        } catch (Exception e) {
            System.err.println("Service: Error updating book: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public void deleteBook(int id) {
        try {
            // Check if book exists
            if (!bookRepository.existsById(id)) {
                throw new RuntimeException("Book not found with id: " + id);
            }
            
            bookRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Service: Error deleting book: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public int getTotalBooksCount() {
        try {
            System.out.println("Service: Getting total books count");
            long count = bookRepository.count();
            System.out.println("Service: Total books count: " + count);
            return (int) count;
        } catch (Exception e) {
            System.err.println("Service: Error getting total books count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    public double getBorrowedBooksPercentage() {
        try {
            System.out.println("Service: Calculating borrowed books percentage");
            long totalBooks = bookRepository.count();
            if (totalBooks == 0) return 0;
            
            long borrowedBooks = bookRepository.countByAvailable(false);
            double percentage = (double) borrowedBooks / totalBooks * 100;
            System.out.println("Service: Borrowed books percentage: " + percentage + "% (" + borrowedBooks + "/" + totalBooks + ")");
            return percentage;
        } catch (Exception e) {
            System.err.println("Service: Error calculating borrowed books percentage: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    public List<Book> getPopularBooks(int limit) {
        try {
            System.out.println("Service: Getting popular books, limit: " + limit);
            // This is a simplified implementation
            // In a real application, you would calculate popularity based on transaction history
            List<Book> books = bookRepository.findTop5ByOrderByIdDesc();
            System.out.println("Service: Found " + books.size() + " popular books");
            return books;
        } catch (Exception e) {
            System.err.println("Service: Error fetching popular books: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}
