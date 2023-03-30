package com.mihak.jumun.menu.service;

import com.mihak.jumun.menu.dto.MenuFormDto;
import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.entity.MenuStock;
import com.mihak.jumun.menu.repository.MenuStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuStockService {

    private final MenuStockRepository menuStockRepository;

    public void save(Menu menu, Long quantity) {

        MenuStock menuStock = MenuStock.builder()
                .menu(menu)
                .quantity(quantity)
                .build();

        menuStockRepository.save(menuStock);
    }

    public MenuStock findByMenu(Menu menu) {
        return menuStockRepository.findByMenu(menu).orElseThrow(RuntimeException::new);
    }
}
