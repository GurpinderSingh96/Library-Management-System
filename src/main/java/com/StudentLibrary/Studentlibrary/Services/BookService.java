package com.StudentLibrary.Studentlibrary.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Repositories.BookRepository;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public void createBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        bookRepository.save(book);
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
}
