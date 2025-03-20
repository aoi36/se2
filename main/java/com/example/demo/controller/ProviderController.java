package com.example.demo.controller;

import com.example.demo.Service.ProviderService;
import com.example.demo.model.Book;
import com.example.demo.model.Provider;
import com.example.demo.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "provider")
public class ProviderController {
    @Autowired
    ProviderRepository providerRepo;
    @Autowired
    ProviderService providerService;

    @GetMapping(value = "/list")
    public String getProviderList(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  Model model){
        Page<Provider> providerPage = providerService.getPaginatedOrders(PageRequest.of(page, size));
        model.addAttribute("providerPage", providerPage);
        return "providerList";
    }
    @GetMapping(value = "detail/{id}")
    public String getProviderDetail(@PathVariable("id") Long id, Model model){
        Provider provider = providerRepo.findById(id).orElse(null);
        if (provider == null) {
            model.addAttribute("provider", null);
             model.addAttribute("books", Collections.emptyList());
             return "providerDetail";
        }
        List<Book> books = provider.getBooks();
        model.addAttribute("provider", provider);
        model.addAttribute("books", books);
        return "providerDetail";
    }
}
