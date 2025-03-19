package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    OrderRepository orderRepo;

    @GetMapping(value = "/list")
    public String getOrderList(Model model){
        List<Order> orders = orderRepo.findAll();
        model.addAttribute("orders", orders);
        return "orderList";
    }
    @GetMapping(value = "/detail/{id}")
    public String getOrderDetail(@PathVariable("id") Long id, Model model){
        Optional<Order> order = orderRepo.findById(id);
        model.addAttribute("order", order);
        return "orderDetail";
    }
    @PostMapping(value = "/save")
    public String saveOrderStatus(@ModelAttribute Order order){
        orderRepo.save(order);
        Long id = order.getId();
        return "redirect:/order/detail/{id}";
    }

}
