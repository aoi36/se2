package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.model.User.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Controller
public class HomeController {
    @Autowired
    OrderRepository orderRepo;
    @Autowired
    UserRepository userRepository;
    @GetMapping("/home")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String homePage(Model model) {
        List<Order> orders = orderRepo.findAll();

        Map<Integer, BigDecimal> revenueByMonth = orders.stream()
                .filter(order -> order.getOrderDate() != null)
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().getMonthValue(),
                        Collectors.mapping(
                                order -> order.getOrderItems().stream()
                                        .map(item -> BigDecimal.valueOf(item.getOrderItemPrice() * item.getOrderItemQuantity()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        List<Integer> months = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            months.add(month);
            revenues.add(revenueByMonth.getOrDefault(month, BigDecimal.ZERO));
        }
        model.addAttribute("months", months);
        model.addAttribute("revenues", revenues);

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Order> recentOrders = orders.stream()
                .filter(order -> order.getOrderDate() != null && order.getOrderDate().isAfter(oneMonthAgo))
                .collect(Collectors.toList());
        model.addAttribute("recentOrders", recentOrders);

        List<User> recentUsers = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null)
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentUsers", recentUsers);

        return "Home/home";
    }
}
