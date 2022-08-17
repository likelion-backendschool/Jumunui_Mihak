package com.mihak.jumun.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Long id;

    private String nickname;
    private LocalDateTime visitedAt;
}
