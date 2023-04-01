package com.mihak.jumun.menu.service;

import com.mihak.jumun.factory.MenuFixture;
import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.entity.MenuStock;
import com.mihak.jumun.menu.repository.MenuRepository;
import com.mihak.jumun.menu.repository.MenuStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MenuStockServiceTest {

    @Autowired
    private MenuStockService menuStockService;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuStockRepository menuStockRepository;

    @DisplayName("재고 감소 - 순차적인 요청")
    @Test
    void decrease_single_thread() {

        // when
        Menu menu = menuRepository.saveAndFlush(MenuFixture.createLimitedMenu());
        menuStockRepository.save(MenuStock.builder()
                .quantity(10L)
                .menu(menu)
                .build());

        // given
        for (int i = 0; i < 5; i++) {
            menuStockService.decreaseQuantity(menu, 1L);
        }

        // then
        MenuStock menuStock = menuStockRepository.findByMenu(menu).orElseThrow();
        assertThat(menuStock.getQuantity()).isEqualTo(5L);
    }

    @DisplayName("재고 감소 - 동시 요청(낙관적 락)")
    @Test
    void decrease_with_optimistic_lock() throws InterruptedException {

        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        Menu menu = menuRepository.saveAndFlush(MenuFixture.createLimitedMenu());
        menuStockRepository.save(MenuStock.builder()
                .quantity(10L)
                .menu(menu)
                .build());

        // given
        for (int i = 0; i < threadCount ; i++) {
            executorService.submit(() -> {
                try {
                    menuStockService.decreaseQuantity(menu, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        MenuStock menuStock = menuStockRepository.findByMenu(menu).orElseThrow();
        assertThat(menuStock.getQuantity()).isEqualTo(0L);
    }
}