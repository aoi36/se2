package com.example.demo.controller;
import com.example.demo.Service.FileStorageService;
import com.example.demo.model.Category;
import com.example.demo.model.User.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.Service.BookService;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/book")
public class bookController {
    @Autowired
    BookRepository bookRepo;
    @Autowired
    BookService bookService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @GetMapping(value = "list")
    public String getAllBooks(
            @RequestParam(value = "attribute", required = false) List<String> sortAttributeColumn,
            @RequestParam(value = "order", required = false) List<String> sortOrder,
            @RequestParam(value = "category", required = false) Long category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            Principal principal) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        boolean isProvider = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROVIDER"));

        if (isProvider) {
            Optional<User> user = userRepository.findByUsername(principal.getName());
            if (user.isPresent()) {
                return "redirect:/book/list/" + user.get().getId();
            }
        }

        if (sortAttributeColumn == null) {
            sortAttributeColumn = new ArrayList<>();
        }
        if (sortOrder == null) {
            sortOrder = new ArrayList<>();
        }

        List<Sort.Order> ordersList = new ArrayList<>();
        for (int i = 0; i < sortAttributeColumn.size(); i++) {
            String attribute = sortAttributeColumn.get(i);
            String order = sortOrder.size() > i ? sortOrder.get(i) : "asc";
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            ordersList.add(new Sort.Order(direction, attribute));
        }
        Sort sort = !ordersList.isEmpty() ? Sort.by(ordersList) : Sort.unsorted();

        Page<Book> bookPage;
        if (category != null && !category.equals(0L)) {
            bookPage = bookService.getBooksByCategory(category, page, size, sort);
            model.addAttribute("selectedCategory", category);
        } else {
            bookPage = bookService.getBooks(page, size, sort);
        }
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("currentPage", bookPage.getNumber());

        return "Book/bookList";
    }
    @GetMapping(value = "list/{id}")
    public String getProviderBook(
            @RequestParam(value = "attribute", required = false) List<String> sortAttributeColumn,
            @RequestParam(value = "order", required = false) List<String> sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @PathVariable(value = "id") Long id,
            Principal principal,
            Model model) {

        User user = userRepository.findById(id).orElse(null);


        if (user == null || !Objects.equals(user.getUsername(), principal.getName())) {
            model.addAttribute("error", "Can't access book list");
            return "Error/error";
        }
        if (sortAttributeColumn == null) {
            sortAttributeColumn = new ArrayList<>();
        }
        if (sortOrder == null) {
            sortOrder = new ArrayList<>();
        }
        List<Sort.Order> ordersList = new ArrayList<>();
        for (int i = 0; i < sortAttributeColumn.size(); i++) {
            String attribute = sortAttributeColumn.get(i);
            String order = sortOrder.size() > i ? sortOrder.get(i) : "asc";
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            ordersList.add(new Sort.Order(direction, attribute));
        }
        Sort sort = !ordersList.isEmpty() ? Sort.by(ordersList) : Sort.unsorted();
        Page<Book> bookPage = bookService.getBooksByUser(user, page, size, sort);


        model.addAttribute("bookPage", bookPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("currentPage", bookPage.getNumber());

        return "Book/bookListProvider";
    }

    @GetMapping("/detail/{id}")
    public String getBookDetail(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "Book/bookDetail";
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean isProvider = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROVIDER"));

        Book book = bookRepo.findById(id).orElse(null);
        if (book == null) {
            model.addAttribute("error", "Book not found");
            return "Book/bookDetail";
        }

        if (isProvider) {
            if (!book.getUser().equals(user)) {
                model.addAttribute("error", "You are not authorized to view this book");
                return "Book/bookDetail";
            }
        }

        Set<Category> activeCategories = book.getCategories().stream()
                .filter(category -> Boolean.TRUE.equals(category.getStatus()))
                .collect(Collectors.toSet());

        model.addAttribute("book", book);
        model.addAttribute("activeCategories", activeCategories);
        return "Book/bookDetail";
    }

    @GetMapping(value = "/add")
    public String addBook(Model model){
        Book book = new Book();
        model.addAttribute("categoriesList", categoryRepository.findAll());

        model.addAttribute("book", book);

        return "Book/addBook";
    }

    @PostMapping("/save")
    public String saveBook(@Valid @ModelAttribute("book") Book book, BindingResult result,
                           @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
                           Model model) {

        if (result.hasErrors()) {
            model.addAttribute("errors", result.getAllErrors());
            model.addAttribute("book", book);
            model.addAttribute("categoriesList", categoryRepository.findAll());
            return "Book/addBook";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "Error/error";
        }
        book.setUser(user);

        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                Path uploadPath = fileStorageService.getFileStorageLocation();
                String fileName = System.currentTimeMillis() + "_" + coverImage.getOriginalFilename();
                try (InputStream inputStream = coverImage.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                book.setBookCover(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        book.setCreatedAt(LocalDateTime.now());
        bookRepo.save(book);
        return "redirect:/book/list";
    }

    @PostMapping("/update")
    public String updateBook(@Valid @ModelAttribute Book book, BindingResult result,
                             @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
                             Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isProvider = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROVIDER"));
        User user = userRepository.findByUsername(auth.getName()).orElse(null);

        if (book.getId() == null) {
            model.addAttribute("error", "Book ID is missing");
            return "Error/error";
        }

        Optional<Book> existingBookOpt = bookRepo.findById(book.getId());
        if (!existingBookOpt.isPresent()) {
            model.addAttribute("error", "Book not found");
            return "Error/error";
        }
        Book existingBook = existingBookOpt.get();

        if (isProvider) {
            if (!auth.getName().equals(existingBook.getUser().getUsername())) {
                model.addAttribute("error", "You are not authorized to update this book");
                return "Error/error";
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("book", book);
            model.addAttribute("categoriesList", categoryRepository.findAll());
            return "Book/updateBook";
        }

        if (user == null) {
            model.addAttribute("error", "User not found");
            return "Error/error";
        }

        book.setCreatedAt(existingBook.getCreatedAt());

        book.setUser(user);

        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                Path uploadPath = fileStorageService.getFileStorageLocation();
                String fileName = System.currentTimeMillis() + "_" + coverImage.getOriginalFilename();

                try (InputStream inputStream = coverImage.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                existingBook.setBookCover(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            existingBook.setBookCover(existingBook.getBookCover());
        }

        book.setUpdatedAt(LocalDateTime.now());

        existingBook.setName(book.getName());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setStockQuantity(book.getStockQuantity());
        existingBook.setPublicationYear(book.getPublicationYear());
        existingBook.setDescription(book.getDescription());
        existingBook.setStatus(book.getStatus());
        existingBook.setUser(book.getUser());
        existingBook.setUpdatedAt(LocalDateTime.now());
        existingBook.setCategories(book.getCategories());

        bookRepo.save(existingBook);
        return "redirect:/book/list";
    }

    @GetMapping(value = "/update/{id}")
    public String updateBook(@PathVariable("id") Long id, Model model) {
        Optional<Book> bookOpt = bookRepo.findById(id);
        if (!bookOpt.isPresent()) {
            model.addAttribute("error", "Book not found");
            return "Book/updateBook";
        }
        Book book = bookOpt.get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isProvider = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROVIDER"));
        if (isProvider && !book.getUser().getUsername().equals(auth.getName())) {
            model.addAttribute("error", "You are not authorized to update this book");
            return "Book/updateBook";
        }
        model.addAttribute("categoriesList", categoryRepository.findAll());
        model.addAttribute("book", book);
        return "Book/updateBook";
    }


    @GetMapping("/search")
    public String searchBooks(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.searchBooks(query != null ? query : "", pageable);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isProvider = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROVIDER"));
        User user = userRepository.findByUsername(auth.getName()).orElse(null);

        List<Book> filteredBooks = books.getContent().stream()
                .filter(book -> book.getUser().equals(user))
                .collect(Collectors.toList());
        books = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());

        if (books.isEmpty()) {
            model.addAttribute("emptyMessage", "No books found");
        }
        model.addAttribute("books", books);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", books.getTotalPages());
        return "Book/searchBookResult";
    }
}

