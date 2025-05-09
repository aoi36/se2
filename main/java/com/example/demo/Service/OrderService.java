package com.example.demo.Service;

import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Page<Order> getPaginatedOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> searchOrders(String orderQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        try {
            Long searchId = Long.parseLong(orderQuery);
            return orderRepository.findByIdOrUserId(searchId, pageable);
        } catch (NumberFormatException e) {
            return Page.empty();
        }
    }
}
