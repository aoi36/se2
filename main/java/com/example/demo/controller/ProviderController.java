package com.example.demo.controller;

import com.example.demo.Service.ProviderService;
import com.example.demo.model.Book;
import com.example.demo.model.Provider;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    @Autowired
    BookRepository bookRepo;

    @GetMapping(value = "/list")
    public String getProviderList(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  Model model){
        page = Math.max(page, 0);
        Page<Provider> providerPage = providerService.getPaginatedOrders(PageRequest.of(page, size));
        model.addAttribute("providerPage", providerPage);
        return "providerList";
    }
    @GetMapping(value = "detail/{id}")
    public String getProviderDetail(@PathVariable("id") Long id,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "5") int size,
                                    Model model) {
        Optional<Provider> providerOptional = providerRepo.findById(id);

        if (providerOptional.isPresent()) {
            Provider provider = providerOptional.get();
            Page<Book> booksPage = bookRepo.findByProvider(provider, PageRequest.of(page, size));

            model.addAttribute("provider", provider);
            model.addAttribute("booksPage", booksPage);
        } else {
            model.addAttribute("provider", null);
            model.addAttribute("booksPage", Page.empty());
        }

        return "providerDetail";
    }
    @GetMapping(value = "/update/{id}")
    public String getUpdatePage(@PathVariable("id") Long id,Model model){
        Provider provider = providerRepo.findById(id).orElse(null);
        if (provider == null) {
            return "redirect:/provider/list";
        }

        model.addAttribute("provider", provider);
        return "providerUpdate";
    }
    @GetMapping(value = "/add")
    public String addProvider(Model model){
        Provider provider = new Provider();
        model.addAttribute("provider", provider);
        return "addProvider";
    }
    @PostMapping("/save")
    public String saveProvider(@ModelAttribute Provider provider,
                               @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {
        Provider existingProvider = providerRepo.findById(provider.getId()).orElse(null);

        if (existingProvider == null) {
            return "redirect:/provider/list"; // Handle non-existing provider
        }

        existingProvider.setProviderName(provider.getProviderName()); // Update only name

        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/images/";
                String fileName = System.currentTimeMillis() + "_" + coverImage.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                try (InputStream inputStream = coverImage.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                existingProvider.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        providerRepo.save(existingProvider);
        return "redirect:/provider/list";
    }
    @PostMapping(value = "delete")
    public String deleteProviders(@RequestParam("selectedIds") List<Long> selectedIds) {
        if (selectedIds != null && !selectedIds.isEmpty()) {
            for (Long selectedId : selectedIds){
                List<Book> books = providerRepo.findById(selectedId).get().getBooks();
                bookRepo.deleteAll(books);
            }
            providerRepo.deleteAllByIdInBatch(selectedIds);
        }
        return "redirect:/provider/list";
    }
    @PostMapping("/delete/{id}")
    public String deleteProvider(@PathVariable("id") Long id) {
        Provider provider = providerRepo.findById(id).orElse(null);
        if (provider != null) {
            // Manually remove associated books
            List<Book> books = provider.getBooks();
            if (books != null) {
                bookRepo.deleteAll(books);
            }
            // Now delete the provider
            providerRepo.delete(provider);
        }
        return "redirect:/provider/list";
    }
}
