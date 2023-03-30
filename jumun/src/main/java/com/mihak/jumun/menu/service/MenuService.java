package com.mihak.jumun.menu.service;

import com.mihak.jumun.category.entity.Category;
import com.mihak.jumun.category.repository.CategoryRepository;
import com.mihak.jumun.category.exception.CategoryNotFoundException;
import com.mihak.jumun.menu.exception.MenuNotFoundException;
import com.mihak.jumun.menu.dto.MenuFormDto;
import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.repository.MenuRepository;
import com.mihak.jumun.store.entity.Store;
import com.mihak.jumun.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final StoreService storeService;

    private final MenuStockService menuStockService;

    @Transactional
    public Menu save(MenuFormDto menuformDto) {
        Menu menu = menuRepository.save(Menu.createMenu(menuformDto));

        if (menuformDto.getIsLimitedSale()) {
            menuStockService.save(menu, menuformDto.getQuantity());
        }

        return menu;
    }

    public Menu findById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new MenuNotFoundException("수정할 메뉴가 없습니다!"));
    }

    public List<Menu> findAllByCategoryId(Long id) {
        return menuRepository.findByCategoryId(id);
    }

    public List<Menu> findAllByCategoryAndStore(Long categoryId, Store store) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);

        return menuRepository.findByCategoryAndStore(category, store);
    }

    public void deleteByMenu(Menu menu) {
        menuRepository.delete(menu);
    }


    public List<Menu> findAllByStore(String storeSN) {
        Store store = storeService.findBySerialNumber(storeSN);
        return menuRepository.findByStore(store);
    }

    public void modify(Long menuId, MenuFormDto menuformDto) {
        Menu menu = findById(menuId);

        menu.changeInfo(menuformDto.getCategory(), menuformDto.getName(), menuformDto.getPrice(), menuformDto.getImgUrl(), menuformDto.getDescription(), menuformDto.getStore());
        menuRepository.save(menu);
    }

    public boolean isMenuDuplicated(String name, Store store) {
        Optional<Menu> menu = menuRepository.findByNameAndStore(name, store);
        if(menu.isPresent()) return true;  // 중복된 메뉴가 있다.
        else return false;

    }

    public boolean isMenuDuplicatedAndDifferentId(String name, Store store, Long menuId) {
        Optional<Menu> menu = menuRepository.findByNameAndIdNot(name, menuId);
        if(menu.isPresent() && isMenuDuplicated(name, store)) return true;
        else return false;
    }
}
