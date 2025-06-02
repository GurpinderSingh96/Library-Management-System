package com.StudentLibrary.Studentlibrary.Services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Model.Transaction;
import com.StudentLibrary.Studentlibrary.Model.TransactionStatus;
import com.StudentLibrary.Studentlibrary.Repositories.BookRepository;
import com.StudentLibrary.Studentlibrary.Repositories.StudentRepository;
import com.StudentLibrary.Studentlibrary.Repositories.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    private static final int MAX_ALLOWED_BOOKS = 3;
    private static final int FINE_PER_DAY = 5;
    
    @Transactional
    public String issueBooks(int studentId, int bookId) throws Exception {
        // Check if book is available
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Book not found with id: " + bookId));
        
        if (!book.isAvailable()) {
            throw new Exception("Book is not available for issue");
        }
        
        // Check if student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new Exception("Student not found with id: " + studentId));
        
        // Check if student has reached max allowed books
        List<Book> issuedBooks = student.getBooks();
        if (issuedBooks != null && issuedBooks.size() >= MAX_ALLOWED_BOOKS) {
            throw new Exception("Student has already issued maximum allowed books");
        }
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setBook(book);
        transaction.setStudent(student);
        transaction.setIsIssueOperation(true);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setTransactionId(UUID.randomUUID().toString());
        
        // Update book status
        book.setAvailable(false);
        book.setStudent(student);
        bookRepository.save(book);
        
        // Save transaction
        transactionRepository.save(transaction);
        
        return transaction.getTransactionId();
    }
    
    @Transactional
    public String returnBooks(int studentId, int bookId) throws Exception {
        // Check if book exists
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Book not found with id: " + bookId));
        
        // Check if student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new Exception("Student not found with id: " + studentId));
        
        // Check if book was issued to this student
        if (book.getStudent() == null || book.getStudent().getId() != studentId) {
            throw new Exception("This book was not issued to this student");
        }
        
        // Find the issue transaction
        List<Transaction> transactions = transactionRepository.findByBookAndStudentAndIsIssueOperationTrueOrderByTransactionDateDesc(
                book, student, PageRequest.of(0, 1));
        
        if (transactions.isEmpty()) {
            throw new Exception("No issue transaction found for this book and student");
        }
        
        Transaction issueTransaction = transactions.get(0);
        
        // Calculate fine
        Date issueDate = issueTransaction.getTransactionDate();
        Date returnDate = new Date();
        
        long diffInMillies = returnDate.getTime() - issueDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        
        int fine = 0;
        if (diffInDays > 15) { // Assuming 15 days is the standard issue period
            fine = (int) ((diffInDays - 15) * FINE_PER_DAY);
        }
        
        // Create return transaction
        Transaction returnTransaction = new Transaction();
        returnTransaction.setBook(book);
        returnTransaction.setStudent(student);
        returnTransaction.setIsIssueOperation(false);
        returnTransaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        returnTransaction.setFineAmount(fine);
        returnTransaction.setTransactionId(UUID.randomUUID().toString());
        
        // Update book status
        book.setAvailable(true);
        book.setStudent(null);
        bookRepository.save(book);
        
        // Save transaction
        transactionRepository.save(returnTransaction);
        
        return returnTransaction.getTransactionId();
    }
    
    public List<Transaction> getAllTransactions() {
        try {
            System.out.println("Service: Getting all transactions");
            List<Transaction> transactions = transactionRepository.findAll(
                Sort.by(Sort.Direction.DESC, "transactionDate")
            );
            System.out.println("Service: Found " + transactions.size() + " transactions");
            return transactions;
        } catch (Exception e) {
            System.err.println("Service: Error fetching all transactions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Transaction getTransactionById(String transactionId) {
        try {
            System.out.println("Service: Getting transaction by ID: " + transactionId);
            Optional<Transaction> transaction = transactionRepository.findByTransactionId(transactionId);
            if (transaction.isPresent()) {
                System.out.println("Service: Found transaction with ID: " + transactionId);
                return transaction.get();
            } else {
                System.out.println("Service: No transaction found with ID: " + transactionId);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Service: Error fetching transaction by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Transaction> getTransactionsByCardId(int cardId) {
        try {
            System.out.println("Service: Getting transactions for card ID: " + cardId);
            // In a real implementation, you would query by card ID
            // For now, we'll get transactions by student ID (assuming card ID = student ID)
            Optional<Student> student = studentRepository.findById(cardId);
            if (student.isPresent()) {
                List<Transaction> transactions = transactionRepository.findByStudent(
                    student.get(),
                    Sort.by(Sort.Direction.DESC, "transactionDate")
                );
                System.out.println("Service: Found " + transactions.size() + " transactions for card ID: " + cardId);
                return transactions;
            } else {
                System.out.println("Service: No student found with ID: " + cardId);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Service: Error fetching transactions by card ID: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<Transaction> getOverdueTransactions() {
        try {
            System.out.println("Service: Getting overdue transactions");
            // In a real implementation, you would query for transactions where:
            // 1. It's an issue transaction
            // 2. No corresponding return transaction exists
            // 3. Issue date is more than X days ago
            
            // For now, return a subset of all transactions as a placeholder
            List<Transaction> allTransactions = transactionRepository.findByIsIssueOperationTrue(
                Sort.by(Sort.Direction.DESC, "transactionDate")
            );
            
            // Filter to only include transactions older than 15 days
            Date fifteenDaysAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15));
            List<Transaction> overdueTransactions = new ArrayList<>();
            
            for (Transaction transaction : allTransactions) {
                if (transaction.getTransactionDate().before(fifteenDaysAgo)) {
                    // Check if there's no return transaction
                    boolean hasReturnTransaction = transactionRepository.existsByBookAndStudentAndIsIssueOperationFalse(
                        transaction.getBook(), transaction.getStudent()
                    );
                    
                    if (!hasReturnTransaction) {
                        overdueTransactions.add(transaction);
                    }
                }
            }
            
            System.out.println("Service: Found " + overdueTransactions.size() + " overdue transactions");
            return overdueTransactions;
        } catch (Exception e) {
            System.err.println("Service: Error fetching overdue transactions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<Transaction> getTransactionsByBookId(int bookId) {
        try {
            System.out.println("Fetching transactions for book ID: " + bookId);
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
            
            List<Transaction> transactions = transactionRepository.findByBook(book, 
                Sort.by(Sort.Direction.DESC, "transactionDate"));
            
            System.out.println("Found " + transactions.size() + " transactions for book ID: " + bookId);
            return transactions;
        } catch (Exception e) {
            System.err.println("Error fetching transactions for book: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public int getTotalTransactionsCount() {
        try {
            System.out.println("Service: Getting total transactions count");
            long count = transactionRepository.count();
            System.out.println("Service: Total transactions count: " + count);
            return (int) count;
        } catch (Exception e) {
            System.err.println("Service: Error getting total transactions count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    public List<Transaction> getRecentTransactions(int limit) {
        try {
            System.out.println("Service: Getting recent transactions, limit: " + limit);
            List<Transaction> transactions = transactionRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "transactionDate"))
            ).getContent();
            System.out.println("Service: Found " + transactions.size() + " recent transactions");
            return transactions;
        } catch (Exception e) {
            System.err.println("Service: Error fetching recent transactions: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    public double getOnTimeReturnsPercentage() {
        try {
            System.out.println("Service: Calculating on-time returns percentage");
            // This is a simplified implementation
            // In a real application, you would calculate based on return dates vs due dates
            
            // For now, return a fixed value
            // In a real implementation, you would query the database to get this information
            double percentage = 92.0;
            System.out.println("Service: On-time returns percentage: " + percentage + "%");
            return percentage;
        } catch (Exception e) {
            System.err.println("Service: Error calculating on-time returns: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
