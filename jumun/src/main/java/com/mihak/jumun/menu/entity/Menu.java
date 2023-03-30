package com.mihak.jumun.menu.entity;

import com.mihak.jumun.category.entity.Category;
import com.mihak.jumun.global.domain.BaseEntity;
import com.mihak.jumun.menuAndOptionGroup.entity.MenuAndOptionGroup;
import com.mihak.jumun.store.entity.Store;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MENU_ID")
    private Long id;

    @NotEmpty
    private String name;
    @NotNull
    private int price;

    @Column(nullable = true)
    private String imgUrl;  //url

    private Boolean isLimitedSale;

    @Lob
    @Column(nullable = true)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    // 메뉴에서 중간테이블 조회
    @OneToMany(mappedBy = "menu")
    private List<MenuAndOptionGroup> menuAndOptionGroups = new ArrayList<>();

    public void addCategory(Category category) {
        this.setCategory(category);
        // 추후 수정. 카테고리에서 메뉴 조회할 일이 필요하다면 양방향 매핑 설정해야 함.
        // (카테고리 삭제 시 안에 있던 메뉴들 이동이 필요할 때?)
    }

    public void addStore(Store store) {   // restaurant 엔티티와 menu 엔티티 연관관계 맺어줌.
        this.setStore(store);
        store.addMenu(this);
    }

    // 메뉴 - 중간테이블(메뉴_메뉴옵션) 연관관계 맺어주기.
    public void addMenuMenuOption(MenuAndOptionGroup menuAndOptionGroup) {
        menuAndOptionGroups.add(menuAndOptionGroup);
        menuAndOptionGroup.setMenu(this);
    }

    // 메뉴 생성 메서드
    public static Menu createMenu(String name, int price, String description, String imgUrl, Category category, Store store) {
        Menu menu = new Menu();
        menu.setCreatedDate(LocalDateTime.now());
        if(!(description.isEmpty())) {
            menu.setDescription(description);
        }
        menu.setName(name);
        menu.setPrice(price);
        menu.addCategory(category);
        menu.addStore(store);

        /* S3처리 후 리턴된 이미지url 저장*/
        menu.setImgUrl(imgUrl);

        return menu;
    }

    public void changeInfo(Category category, String name, Integer price, String imgUrl, String description, Store store) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.imgUrl = imgUrl;
        this.description = description;
        this.store = store;
    }
}
