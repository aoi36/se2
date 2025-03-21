package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer orderItemQuantity;
    private Double orderItemPrice;
    private String bookName;

    @ManyToOne
    private OrderInformation orderInformation;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
}
