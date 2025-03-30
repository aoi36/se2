package com.example.demo.Service;

import com.example.demo.model.Book;
import com.example.demo.model.User.User;
import com.example.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;


    public Page<Book> getBooks(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookRepository.findAll(pageable);
    }
    public Page<Book> searchBooks(String query, Pageable pageable) {
        return bookRepository.searchByNameOrAuthor(query, pageable);
    }
    public Page<Book> getBooksByUser(User user, int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookRepository.findByUser(user, pageable);
    }

    public Page<Book> getBooksByCategory(Long categoryId, int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookRepository.findDistinctByCategories_Id(categoryId, pageable);
    }
}
