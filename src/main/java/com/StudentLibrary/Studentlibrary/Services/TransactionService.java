package com.StudentLibrary.Studentlibrary.Services;

import com.StudentLibrary.Studentlibrary.Model.*;
import com.StudentLibrary.Studentlibrary.Repositories.BookRepository;
import com.StudentLibrary.Studentlibrary.Repositories.StudentRepository;
import com.StudentLibrary.Studentlibrary.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;
    
    @Autowired
    BookRepository bookRepository;
    
    @Autowired
    StudentRepository studentRepository;

    @Value("${books.max_allowed_days}")
    int max_days_allowed;
    
    @Value("${books.fine.per_day}")
    int fine_per_day;

    public String issueBooks(int studentId, int bookId) throws Exception {
        Book book = bookRepository.findById(bookId).get();
        
        if (book == null || !book.isAvailable()) {
            throw new Exception("Book is either unavailable or not present!");
        }
        
        Student student = studentRepository.findById(studentId).get();
        if (student == null) {
            throw new Exception("Student is not registered!");
        }

        book.setAvailable(false);
        book.setStudent(student);
        List<Book> books = student.getBooks();
        books.add(book);
        student.setBooks(books);
        
        bookRepository.updateBook(book);
        
        Transaction transaction = new Transaction();
        transaction.setStudent(student);
        transaction.setBook(book);
        transaction.setIsIssueOperation(true);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transactionRepository.save(transaction);
        
        return transaction.getTransactionId();
    }

    public String returnBooks(int studentId, int bookId) throws Exception {
        List<Transaction> transactions = transactionRepository.findByStudent_Book(studentId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction lastIssueTransaction = transactions.get(transactions.size() - 1);
        
        Date issueDate = lastIssueTransaction.getTransactionDate();
        Long issueTime = Math.abs(issueDate.getTime() - System.currentTimeMillis());
        long numberOfDaysPassed = TimeUnit.DAYS.convert(issueTime, TimeUnit.MILLISECONDS);
        
        int fine = 0;
        if (numberOfDaysPassed > max_days_allowed) {
            fine = (int) Math.abs(numberOfDaysPassed - max_days_allowed) * fine_per_day;
        }
        
        Student student = lastIssueTransaction.getStudent();
        Book book = lastIssueTransaction.getBook();
        book.setStudent(null);
        book.setAvailable(true);
        bookRepository.updateBook(book);
        
        Transaction newTransaction = new Transaction();
        newTransaction.setBook(book);
        newTransaction.setStudent(student);
        newTransaction.setFineAmount(fine);
        newTransaction.setIsIssueOperation(false);
        newTransaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transactionRepository.save(newTransaction);
        
        return newTransaction.getTransactionId();
    }
}
