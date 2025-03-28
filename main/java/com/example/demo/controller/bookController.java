package com.example.demo.controller;
import com.example.demo.Service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.Service.BookService;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/book")
public class bookController {
    @Autowired
    BookRepository bookRepo;
    @Autowired
    BookService bookService;
    @Autowired
    private FileStorageService fileStorageService;


    @GetMapping(value = "list")
    public String getAllBooks(
            @RequestParam(value = "attribute", required = false) List<String> sortAttributeColumn,
            @RequestParam(value = "order", required = false) List<String> sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {


        if (sortAttributeColumn == null) {
            sortAttributeColumn = new ArrayList<>();
        }
        if (sortOrder == null) {
            sortOrder = new ArrayList<>();
        }


        List<Sort.Order> ordersList = new ArrayList<>();
        if (!sortAttributeColumn.isEmpty()) {
            for (int i = 0; i < sortAttributeColumn.size(); i++) {
                String attribute = sortAttributeColumn.get(i);
                String order = sortOrder.size() > i ? sortOrder.get(i) : "asc";
                Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
                ordersList.add(new Sort.Order(direction, attribute));
            }
        }

        Sort sort = !ordersList.isEmpty() ? Sort.by(ordersList) : Sort.unsorted();
        Page<Book> bookPage = bookService.getBooks(page, size, sort);
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("currentPage", bookPage.getNumber());
        return "Book/bookList";
    }
    @GetMapping("/detail/{id}")
    public String getBookDetail(@PathVariable(value = "id") Long id, Model model) {
        Book book = bookRepo.findById(id).orElse(null);


        if (book == null){
            model.addAttribute("error", "book not found");
            return "bookDetail";
        }
        model.addAttribute("book", book);


        return "Book/bookDetail";
    }

    @GetMapping(value = "/add")
    public String addBook(Model model){
        Book book = new Book();

        model.addAttribute("book", book);

        return "Book/addBook";
    }
    @PostMapping("/save")
    public String saveBook(@Valid @ModelAttribute("book") Book book, BindingResult result,
                           @RequestParam(value = "coverImage", required = false) MultipartFile coverImage, Model model) {

        if (result.hasErrors()) {

            model.addAttribute("book", book);
            return "Book/addBook";
        }
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

        bookRepo.save(book);
        return "redirect:/book/list";
    }
    @PostMapping("/update")
    public String updateBook(@Valid @ModelAttribute("book") Book book, BindingResult result,
                           @RequestParam(value = "coverImage", required = false) MultipartFile coverImage, Model model) {

        if (result.hasErrors()) {

            model.addAttribute("book", book);
            return "Book/updateBook";
        }
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

        bookRepo.save(book);
        return "redirect:/book/list";
    }

    @GetMapping(value = "/update/{id}")
    public String updateBook(@PathVariable(value = "id") Long id, Model model){
        Book book = bookRepo.getReferenceById(id);

        model.addAttribute("book", book);

        return "Book/updateBook";
    }
    @PostMapping(value = "delete")
    public String deleteBooks(@RequestParam(value = "selectedIds") List<Long> selectedIds){
        if (selectedIds != null && !selectedIds.isEmpty()) {
            bookRepo.deleteAllByIdInBatch(selectedIds);
        }
        return "redirect:/book/list";
    }

//    @PostMapping(value = "/search")
//    public String searchBookByName(@RequestParam(value = "query", required = false, defaultValue = "") String query, Model model){
//        List<Book> books = bookRepo.findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
//        if (books.isEmpty()){
//            model.addAttribute("msg","No Book Found");
//        }
//        model.addAttribute("books", books);
//        return "searchBookResult";
//    }



    @GetMapping("/search")
    public String searchBooks(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.searchBooks(query != null ? query : "", pageable);
        model.addAttribute("books", books);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", books.getTotalPages());
        return "Book/searchBookResult";
    }

    @PostMapping(value = "delete/{id}")
    public String deleteBooks(@PathVariable("id") Long id){
            bookRepo.deleteById(id);
        return "redirect:/book/list";
    }
}

