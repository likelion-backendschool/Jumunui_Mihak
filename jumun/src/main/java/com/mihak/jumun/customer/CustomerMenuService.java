package com.mihak.jumun.customer;

import com.mihak.jumun.customer.dto.MenuDetailFormDto;
import com.mihak.jumun.entity.Menu;
import com.mihak.jumun.menu.MenuService;
import com.mihak.jumun.optionGroup.OptionGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomerMenuService {
    private final OptionGroupService optionGroupService;
    private final MenuService menuService;

    public MenuDetailFormDto getMenuFormById(Long id) {
        Menu menu = menuService.findById(id);
        MenuDetailFormDto menuDetailFormDto = MenuDetailFormDto.builder()
                .category(menu.getCategory().getName())
                .name(menu.getName())
                .imgUrl(menu.getImgUrl())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .optionGroups(optionGroupService.getOptionGroupsByMenu(menu))
                .checkOptions(new ArrayList<>())
                .build();

        return menuDetailFormDto;
    }

}
