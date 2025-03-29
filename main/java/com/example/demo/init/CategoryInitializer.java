package com.example.demo.init;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class CategoryInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Fiction"));
            categoryRepository.save(new Category("Non-Fiction"));
            categoryRepository.save(new Category("Science"));
            categoryRepository.save(new Category("History"));
            categoryRepository.save(new Category("Biography"));
            System.out.println("Categories initialized.");
        }
    }
}

