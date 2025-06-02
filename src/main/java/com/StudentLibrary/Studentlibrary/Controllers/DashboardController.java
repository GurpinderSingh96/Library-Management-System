package com.StudentLibrary.Studentlibrary.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Model.Transaction;
import com.StudentLibrary.Studentlibrary.Services.AuthorService;
import com.StudentLibrary.Studentlibrary.Services.BookService;
import com.StudentLibrary.Studentlibrary.Services.StudentService;
import com.StudentLibrary.Studentlibrary.Services.TransactionService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get counts
            int totalBooks = bookService.getTotalBooksCount();
            int totalStudents = studentService.getTotalStudentsCount();
            int totalAuthors = authorService.getTotalAuthorsCount();
            int totalTransactions = transactionService.getTotalTransactionsCount();
            
            // Get percentages for statistics
            double borrowedBooksPercentage = bookService.getBorrowedBooksPercentage();
            double studentEngagementPercentage = studentService.getStudentEngagementPercentage();
            double onTimeReturnsPercentage = transactionService.getOnTimeReturnsPercentage();
            
            // Build response
            stats.put("totalBooks", totalBooks);
            stats.put("totalStudents", totalStudents);
            stats.put("totalAuthors", totalAuthors);
            stats.put("totalTransactions", totalTransactions);
            
            stats.put("borrowedBooksPercentage", borrowedBooksPercentage);
            stats.put("studentEngagementPercentage", studentEngagementPercentage);
            stats.put("onTimeReturnsPercentage", onTimeReturnsPercentage);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch dashboard stats: " + e.getMessage()));
        }
    }
    
    @GetMapping("/recent-transactions")
    public ResponseEntity<?> getRecentTransactions(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Transaction> transactions = transactionService.getRecentTransactions(limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch recent transactions: " + e.getMessage()));
        }
    }
    
    @GetMapping("/popular-books")
    public ResponseEntity<?> getPopularBooks(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Book> books = bookService.getPopularBooks(limit);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch popular books: " + e.getMessage()));
        }
    }
}
