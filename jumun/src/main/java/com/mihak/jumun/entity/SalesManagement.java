package com.mihak.jumun.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SalesManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SALEMANAGEMENT_ID")
    private Long id;

    private LocalDateTime date;
    private int totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID")
    private Store restaurant;
}
