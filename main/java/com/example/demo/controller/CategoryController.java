package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;
    @GetMapping(value = "list")
    public String getCategoryList(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "Category/categoryList";
    }

    @GetMapping(value = "add")
    public String getAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "Category/addCategory";
    }
    @GetMapping(value = "update/{id}")
    public String getUpdateCategoryForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            model.addAttribute("category", category);
        }
        return "Category/updateCategory";
    }


    @PostMapping(value = "save")
    public String saveCategory(@Valid @ModelAttribute Category category, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("category", category);
            return "Category/addCategory";
        }
        categoryRepository.save(category);
        return "redirect:/category/list";
    }


    @PostMapping(value = "update")
    public String updateCategory(@Valid @ModelAttribute Category category, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("category", category);
            return "Category/updateCategory";
        }
        categoryRepository.save(category);
        return "redirect:/category/list";
    }

}
