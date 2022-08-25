package com.mihak.jumun.entity;

import javax.persistence.*;

@Entity
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STORE_ID")
    private Long id;

    private String name;
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID")
    private Owner owner;
}