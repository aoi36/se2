package com.example.demo.init;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Component
public class CategoryInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();

            categoryRepository.save(new Category("Fiction", true, now));
            categoryRepository.save(new Category("Non-Fiction", true, now));
            categoryRepository.save(new Category("Science", true, now));
            categoryRepository.save(new Category("History", true, now));
            categoryRepository.save(new Category("Biography", true, now));
            System.out.println("Categories initialized.");
        }
    }
}

