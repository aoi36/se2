package com.example.demo.model;

import com.example.demo.constant.OrderStatus;
import com.example.demo.model.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;



    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @ManyToOne
    private User user;


}