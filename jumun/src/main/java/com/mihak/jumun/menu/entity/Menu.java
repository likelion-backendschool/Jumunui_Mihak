package com.mihak.jumun.menu.entity;

import com.mihak.jumun.category.entity.Category;
import com.mihak.jumun.global.domain.BaseEntity;
import com.mihak.jumun.menu.dto.MenuFormDto;
import com.mihak.jumun.menuAndOptionGroup.entity.MenuAndOptionGroup;
import com.mihak.jumun.store.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MENU_ID")
    private Long id;

    @NotEmpty
    private String name;

    @NotNull
    private int price;

    private String imgUrl;

    private Boolean isLimitedSale;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    // 메뉴에서 중간테이블 조회
    @Builder.Default
    @OneToMany(mappedBy = "menu")
    private List<MenuAndOptionGroup> menuAndOptionGroups = new ArrayList<>();

    // 메뉴 생성 메서드
    public static Menu createMenu(MenuFormDto menuFormDto) {
        return Menu.builder()
                .description(menuFormDto.getDescription())
                .name(menuFormDto.getName())
                .price(menuFormDto.getPrice())
                .category(menuFormDto.getCategory())
                .store(menuFormDto.getStore())
                .imgUrl(menuFormDto.getImgUrl())
                .build();
    }

    public void changeInfo(Category category, String name, Integer price, String imgUrl, String description, Store store) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.imgUrl = imgUrl;
        this.description = description;
        this.store = store;
    }

    public void deleteCategory() {
        this.category = null;
    }

    public MenuFormDto toFormDto(Menu menu) {
        return MenuFormDto.builder()
                .category(menu.getCategory())
                .name(menu.getName())
                .price(menu.getPrice())
                .imgUrl(menu.getImgUrl())
                .description(menu.getDescription())
                .store(menu.getStore())
                .isLimitedSale(menu.getIsLimitedSale())
                .build();
    }
}
