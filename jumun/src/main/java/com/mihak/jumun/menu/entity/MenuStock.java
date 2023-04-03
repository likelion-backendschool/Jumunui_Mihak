package com.mihak.jumun.menu.entity;

import com.mihak.jumun.menu.exception.MenuInsufficientQuantityException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MENUSTOCK_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_ID")
    private Menu menu;

    private Long quantity;

    @Version
    private Long version;

    public boolean isEnoughCount(Long count) {
        return (this.quantity - count) >= 0;
    }

    public void decrease(Long count) {
        if (!isEnoughCount(count)) {
            throw new MenuInsufficientQuantityException();
        }
        this.quantity -= count;
    }

    public void increase(Long count) {
        this.quantity += count;
    }
}
