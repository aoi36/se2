package com.example.demo.controller;

import com.example.demo.Service.AdminService;
import com.example.demo.Service.BookService;
import com.example.demo.model.Administrator;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/book")
public class bookController {
    @Autowired
    BookRepository bookRepo;
    @Autowired
    BookService bookService;

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
        return "bookList";
    }

    @GetMapping("/detail/{id}")
    public String getBookDetail(@PathVariable(value = "id") Long id, Model model) {
        Book book = bookRepo.findById(id).orElse(null);

        if (book == null){
            model.addAttribute("error", "book not found");
            return "bookDetail";
        }
        model.addAttribute("book", book);
        return "bookDetail";
    }

    @GetMapping(value = "/add")
    public String addBook(Model model){
        Book book = new Book();
        model.addAttribute("book", book);
        return "addBook";
    }
    @PostMapping(value = "/save")
    public String saveBook(@ModelAttribute Book book){
        bookRepo.save(book);
        return "redirect:/book/list";
    }

    @GetMapping(value = "/update/{id}")
    public String updateBook(@PathVariable(value = "id") Long id, Model model){
        Book book = bookRepo.getReferenceById(id);
        model.addAttribute("book", book);
        return "updateBook";
    }

    @PostMapping(value = "delete")
    public String deleteBooks(@RequestParam(value = "selectedIds") List<Long> selectedIds){
        if (selectedIds != null && !selectedIds.isEmpty()) {
            bookRepo.deleteAllByIdInBatch(selectedIds);
        }
        return "redirect:/book/list";
    }

    @PostMapping(value = "/search")
    public String searchBookByName(@RequestParam(value = "query", required = false, defaultValue = "") String query, Model model){
        List<Book> books = bookRepo.findByNameContainingIgnoreCase(query);
        if (books.isEmpty()){
            model.addAttribute("msg","No Book Found");
        }
        model.addAttribute("books", books);
        return "searchBookResult";
    }

}
