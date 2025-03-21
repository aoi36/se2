package com.example.demo.controller;

import com.example.demo.Service.OrderService;
import com.example.demo.constant.OrderStatus;
import com.example.demo.model.Administrator;
import com.example.demo.model.OrderInformation;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public String getOrderList(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model){
        Page<OrderInformation> orderPage = orderService.getPaginatedOrders(PageRequest.of(page, size));
        model.addAttribute("orderPage", orderPage);
        return "orderList";
    }

    @GetMapping(value = "/detail/{id}")
    public String getOrderDetail(@PathVariable("id") Long id, Model model){
        OrderInformation order = orderRepo.findById(id).orElse(null);
        model.addAttribute("order", order);
        return "orderDetail";
    }
    @PostMapping("/order/save")
    public String saveOrderStatus(@ModelAttribute OrderInformation orderInformation, Principal principal, Model model) {
        OrderInformation existingOrder = orderRepo.findById(orderInformation.getId()).orElse(null);
        if (existingOrder == null) {
            return "redirect:/order/list";
        }

        String loggedInAdminUsername = principal.getName();

        if (!existingOrder.getAdministrator().getUsername().equals(loggedInAdminUsername)) {
            model.addAttribute("errorMessage", "You are not authorized to modify this order.");
            model.addAttribute("order", existingOrder);
            return "orderDetail";
        }
        existingOrder.setStatus(orderInformation.getStatus());
        orderRepo.save(existingOrder);
        model.addAttribute("order", existingOrder);
        return "redirect:/order/detail/" + existingOrder.getId();
    }



}
