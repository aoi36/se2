package com.example.demo.controller;

import com.example.demo.Service.OrderService;
import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@Controller
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    OrderRepository orderRepo;
    @Autowired
    OrderService orderService;

    @GetMapping(value = "/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getOrderList(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model){
        Page<Order> orderPage = orderService.getPaginatedOrders(PageRequest.of(page, size));
        model.addAttribute("orderPage", orderPage);
        return "Order/orderList";
    }

    @GetMapping(value = "/detail/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getOrderDetail(@PathVariable("id") Long id, Model model){
        Order order = orderRepo.findById(id).orElse(null);
        model.addAttribute("order", order);
        return "Order/orderDetail";
    }
    @PostMapping("/save")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveOrderStatus(@ModelAttribute Order order, Principal principal, Model model) {
        Order existingOrder = orderRepo.findById(order.getId()).orElse(null);
        if (existingOrder == null) {
            return "redirect:/order/list";
        }
        String loggedInAdminUsername = principal.getName();

        if (!existingOrder.getUser().getUsername().equals(loggedInAdminUsername)) {
            model.addAttribute("errorMessage", "You are not authorized to modify this order.");
            model.addAttribute("order", existingOrder);
            return "Order/orderDetail";
        }
        existingOrder.setStatus(order.getStatus());
        orderRepo.save(existingOrder);
        model.addAttribute("order", existingOrder);
        return "redirect:/order/detail/" + existingOrder.getId();
    }
}
