package com.mihak.jumun.entity;

import javax.persistence.*;

@Entity
public class MenuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MENUOPTION_ID")
    private Long id;

    private String name;
    private String price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID")
    private Store restaurant;
}
