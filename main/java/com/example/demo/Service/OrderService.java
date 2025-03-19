package com.example.demo.Service;

import com.example.demo.model.OrderInformation;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Page<OrderInformation> getPaginatedOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
