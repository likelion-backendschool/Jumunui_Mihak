package com.mihak.jumun.factory;

import com.mihak.jumun.menu.entity.Menu;

public class MenuFixture {

    public static Menu createLimitedMenu() {
        return Menu.builder()
                .name("test_menu")
                .isLimitedSale(true)
                .price(1000)
                .build();
    }
}
