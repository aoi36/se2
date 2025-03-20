package com.example.demo.model;

import com.example.demo.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class OrderInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;


    private LocalDateTime orderDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @OneToMany(mappedBy = "orderInformation")
    private List<OrderItem> orderItemList;
    @ManyToOne
    private Administrator administrator;

}