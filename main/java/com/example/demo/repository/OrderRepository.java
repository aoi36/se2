package com.example.demo.repository;

import com.example.demo.model.OrderInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderInformation, Long> {
    Page<OrderInformation> findAll(Pageable pageable);
}
