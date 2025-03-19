package com.example.demo.repository;


import com.example.demo.model.Book;
import com.example.demo.model.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findById(Long id);
    Page<Provider> findAll(Pageable pageable);
}
