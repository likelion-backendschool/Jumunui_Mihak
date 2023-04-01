package com.mihak.jumun.menu.service;

import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.entity.MenuStock;
import com.mihak.jumun.menu.exception.MenuStockNotFoundException;
import com.mihak.jumun.menu.repository.MenuStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuStockService {

    private final MenuStockRepository menuStockRepository;

    @Transactional
    public MenuStock save(Menu menu, Long quantity) {

        MenuStock menuStock = MenuStock.builder()
                .menu(menu)
                .quantity(quantity)
                .build();

        return menuStockRepository.save(menuStock);
    }

    public MenuStock findByMenu(Menu menu) {
        return menuStockRepository.findByMenu(menu).orElseThrow(MenuStockNotFoundException::new);
    }

    public Boolean remainsQuantity(Menu menu, Long count) {
        MenuStock menuStock = findByMenu(menu);

        return menuStock.isEnoughCount(count);
    }

    @Transactional
    public void decreaseQuantity(Menu menu, Long count) {
        MenuStock menuStock = menuStockRepository.findByMenuWithPessimisticLock(menu).orElseThrow(MenuStockNotFoundException::new);
        menuStock.decrease(count);
    }
}
