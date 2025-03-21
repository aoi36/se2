package com.example.demo.repository;

import com.example.demo.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(String name, String author);
    void deleteAllByIdInBatch(Iterable<Long> ids);
    Page<Book> findAll(Pageable pageable);
}
