package com.example.demo.repository;

import com.example.demo.model.Book;
import com.example.demo.model.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    void deleteAllByIdInBatch(Iterable<Long> ids);
    Page<Book> findAll(Pageable pageable);
    Page<Book> findByProvider(Provider provider, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> searchByNameOrAuthor(@Param("query") String query, Pageable pageable);
}
