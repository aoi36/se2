package com.example.demo.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date orderDate;
    private Integer totalAmount;
    private String status;
    @ManyToOne
    private Customer customer;
    @ManyToOne
    @JoinColumn(name = "administrator_id")
    private Administrator administrator;
    @OneToMany(mappedBy = "order")
    private List<OrderDetail> orderDetails;
}
