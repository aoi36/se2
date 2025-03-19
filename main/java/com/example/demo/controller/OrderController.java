package com.example.demo.controller;

import com.example.demo.Service.OrderService;
import com.example.demo.model.OrderInformation;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;



@Controller
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    OrderRepository orderRepo;
    @Autowired
    OrderService orderService;

    @GetMapping(value = "/list")
    public String getOrderList(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model){
        Page<OrderInformation> orderPage = orderService.getPaginatedOrders(PageRequest.of(page, size));
        model.addAttribute("orderPage", orderPage);
        return "orderList";
    }

    @GetMapping(value = "/detail/{id}")
    public String getOrderDetail(@PathVariable("id") Long id, Model model){
        Optional<OrderInformation> order = orderRepo.findById(id);
        model.addAttribute("order", order);
        return "orderDetail";
    }
    @PostMapping(value = "/save")
    public String saveOrderStatus(@ModelAttribute OrderInformation orderInformation){
        orderRepo.save(orderInformation);
        Long id = orderInformation.getId();
        return "redirect:/order/detail/{id}";
    }

}
