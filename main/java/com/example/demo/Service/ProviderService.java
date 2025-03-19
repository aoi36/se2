package com.example.demo.Service;

import com.example.demo.model.Provider;
import com.example.demo.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProviderService {
    @Autowired
    private ProviderRepository orderRepository;

    public Page<Provider> getPaginatedOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
