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
                           @RequestParam("coverImage") MultipartFile coverImage) {
        if (!coverImage.isEmpty()) {
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
                provider.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        providerRepo.save(provider);
        return "redirect:/provider/list";
    }

    @PostMapping(value = "delete")
    public String deleteProviders(@RequestParam("selectedIds") List<Long> selectedIds) {
        if (selectedIds != null && !selectedIds.isEmpty()) {
            providerRepo.deleteAllByIdInBatch(selectedIds);
        }
        return "redirect:/provider/list";
    }
}
