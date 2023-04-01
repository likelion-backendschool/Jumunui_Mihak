package com.mihak.jumun.menu.service;

import com.mihak.jumun.menu.dto.MenuFormDto;
import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.store.entity.Store;
import com.mihak.jumun.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@ActiveProfiles("test")
class MenuServiceTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuStockService menuStockService;

    @BeforeEach
    void beforeEach() {
        storeRepository.save(new Store(1L, "test", null, "1234", null, null));
    }

    @Test
    @DisplayName(value = "한정판매가 아닌 메뉴 저장")
    @Rollback
    void saveNotLimitedMenu() {

        // given
        MenuFormDto menuFormDto = getMenuForm(false, null);

        // when
        Menu menu = menuService.save(menuFormDto);

        // then
        Menu findMenu = menuService.findById(menu.getId());

        assertAll(
                () -> assertThat(menu.equals(findMenu)),
                () -> assertThatThrownBy(() -> menuStockService.findByMenu(menu))
        );
    }

    @Test
    @DisplayName(value = "한정판매인 메뉴 저장")
    @Rollback
    void saveLimitedMenu() {

        // given
        MenuFormDto menuFormDto = getMenuForm(true, 10L);

        // when
        Menu menu = menuService.save(menuFormDto);

        // then
        Menu findMenu = menuService.findById(menu.getId());

        assertAll(
                () -> assertThat(menu.equals(findMenu)),
                () -> assertThat(menuStockService.findByMenu(menu).getQuantity().equals(10L))
        );
    }

    private MenuFormDto getMenuForm(Boolean isLimited, Long quantity) {
        return MenuFormDto.builder()
                .isLimitedSale(isLimited)
                .store(storeRepository.findBySerialNumber("1234"))
                .description("test_description")
                .imgUrl("test_img")
                .price(1000)
                .name("test_name")
                .category(null)
                .optionGroupId(null)
                .quantity(quantity)
                .build();
    }
}