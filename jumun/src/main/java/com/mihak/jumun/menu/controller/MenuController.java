package com.mihak.jumun.menu.controller;

import com.mihak.jumun.category.entity.Category;
import com.mihak.jumun.global.aws.S3Service;
import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.service.MenuService;
import com.mihak.jumun.menu.dto.MenuFormDto;
import com.mihak.jumun.menuAndOptionGroup.service.MenuAndOptionGroupService;
import com.mihak.jumun.optionGroup.entity.OptionGroup;
import com.mihak.jumun.optionGroup.service.OptionGroupService;
import com.mihak.jumun.store.entity.Store;
import com.mihak.jumun.store.service.StoreService;
import com.mihak.jumun.storeCategory.service.SCService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final StoreService storeService;
    private final OptionGroupService optionGroupService;
    private final MenuAndOptionGroupService menuAndOptionGroupService;
    private final SCService scService;
    /*S3 처리*/
    private final S3Service s3Service;

    /* 메뉴 생성폼 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/menu")
    public String showCreateForm(@PathVariable String storeSN, Model model) {
        Store store = storeService.findBySerialNumber(storeSN);

        model.addAttribute("categoryList", scService.findAllbyStoreId(store.getId()));
        model.addAttribute("menuFormDto", new MenuFormDto());
        return "menu/create_menu";
    }
    /* 메뉴 생성 */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{storeSN}/admin/store/menu")
    public String create(@PathVariable("storeSN") String storeSN, @Valid MenuFormDto menuForm, BindingResult result, MultipartFile file) throws IOException {
        // 메뉴명 Null 값, 가격 Null 값 예외 체크
        if (result.hasErrors()) {
            return "menu/create_menu";
        }
        // 메뉴명 중복 체크.
        Store store = storeService.findBySerialNumber(storeSN);
        menuForm.setStore(store);
        // 여기다가 중복 로직 추가.

        boolean isMenuDuplicated = menuService.isMenuDuplicated(menuForm.getName(), menuForm.getStore());
        if (isMenuDuplicated) {
            result.rejectValue("name", "duplicatedMenu", "이미 똑같은 메뉴가 있습니다.");
            return "menu/create_menu";
        }

        if (!file.isEmpty()) {
            String imgPath = s3Service.upload(file);
            menuForm.setImgUrl(imgPath);
        } else {
            menuForm.setImgUrl("https://jumun-bucket.s3.ap-northeast-2.amazonaws.com/readyForMenu.png");
        }

        menuService.save(menuForm);
        return "redirect:/" + store.getSerialNumber() + "/admin/store/menuList";
    }

    // 관리자 메뉴 화면 보여주기 .
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/menuList")
    public String showMenuList(@PathVariable String storeSN, Model model) {
        Store store = storeService.findBySerialNumber(storeSN);

        model.addAttribute("categoryList", scService.findAllbyStoreId(store.getId()));
        model.addAttribute("menuList", menuService.findAllByStore(storeSN));
        model.addAttribute("storeSN", storeSN);

        return "menu/menu_list";
    }
    /*기본, 관리자가 카테고리를 눌렀을 때*/
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{storeSN}/admin/store/menuList")
    @ResponseBody
    public String changeMenuViewByCategory(@RequestParam Long categoryId, @PathVariable("storeSN") String storeSN, Model model) {
        Store store = storeService.findBySerialNumber(storeSN);
        model.addAttribute("categoryList", scService.findAllbyStoreId(store.getId()));
        model.addAttribute("menuList" , menuService.findAllByCategoryId(categoryId));
        model.addAttribute("storeSN", storeSN);

        return "redirect:/" +storeSN+ "/admin/store/menu_list"+categoryId;
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/menuList/{categoryId}")
    public String menuView(@PathVariable("storeSN") String storeSN, @PathVariable("categoryId") Long categoryId, Model model) {
        Store store = storeService.findBySerialNumber(storeSN);
        model.addAttribute("categoryList", scService.findAllbyStoreId(store.getId()));
        model.addAttribute("categoryId" , categoryId);
        model.addAttribute("menuList" , menuService.findAllByCategoryAndStore(categoryId, store));
        model.addAttribute("storeSN", storeSN);

        return "menu/menu_list";
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{storeSN}/admin/store/menuList/{categoryId}")
    @ResponseBody
    public String changeMenuViewByAnotherCategory(@PathVariable("categoryId") Long categoryId, @PathVariable("storeSN") String storeSN, Model model) {
        Store store = storeService.findBySerialNumber(storeSN);
        model.addAttribute("categoryList", scService.findAllbyStoreId(store.getId()));
        model.addAttribute("menuList" , menuService.findAllByCategoryId(categoryId));
        model.addAttribute("storeSN", storeSN);

        return "redirect:/" +storeSN+ "/admin/store/menu_list"+categoryId;
    }

    /* 메뉴 수정 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/menu/modify/{menuId}")
    public String showModifyForm(@PathVariable String storeSN, @PathVariable Long menuId, Model model) {
        Store store = storeService.findBySerialNumber(storeSN);

        model.addAttribute("categoryList", scService.findAllbyStoreId(store.getId()));
        model.addAttribute("optionGroupList", optionGroupService.findAllByStore(store));
        model.addAttribute("optionGroups", menuAndOptionGroupService.getOptionGroupsByMenu(menuService.findById(menuId)));

        Menu menu = menuService.findById(menuId);
        MenuFormDto menuFormDto = menu.toFormDto(menu);

        model.addAttribute("menuFormDto", menuFormDto);
        return "menu/modify_menu";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{storeSN}/admin/store/menu/modify/{menuId}")
    public String modify(@PathVariable("storeSN") String storeSN,
                         @PathVariable Long menuId,
                         @Valid MenuFormDto menuformDto,
                         BindingResult result,
                         MultipartFile file) throws IOException {
        // 메뉴명 Null 값, 가격 Null 값 예외 체크
        if (result.hasErrors()) {
            return "menu/modify_menu";
        }
        // 메뉴명 중복 체크.
        Store store = storeService.findBySerialNumber(storeSN);
        menuformDto.setStore(store);
        boolean isMenuDuplicatedAndDifferentId = menuService.isMenuDuplicatedAndDifferentId(menuformDto.getName(), menuformDto.getStore(), menuId);
        if (isMenuDuplicatedAndDifferentId) {
            result.rejectValue("name", "duplicatedMenu", "이미 똑같은 메뉴가 있습니다.");
            return "menu/modify_menu";
        }

        Menu findMenu = menuService.findById(menuId);
        if (!file.isEmpty()) {
            /*S3 컨트롤러 부분*/
            String imgPath = s3Service.upload(file);
            /*menuForm의 변수에 S3처리 후 리턴된 Url을 넣어주는 코드*/
            menuformDto.setImgUrl(imgPath);
        }else if (file.isEmpty() && findMenu.getImgUrl().equals("https://jumun-bucket.s3.ap-northeast-2.amazonaws.com/readyForMenu.png") ) {
            menuformDto.setImgUrl("https://jumun-bucket.s3.ap-northeast-2.amazonaws.com/readyForMenu.png");
        }else
            menuformDto.setImgUrl(findMenu.getImgUrl());

        menuService.modify(menuId, menuformDto);

        OptionGroup optionGroup = optionGroupService.findByIdAndStore(menuformDto.getOptionGroupId(), store);
        if(!(optionGroup == null)) {
            menuAndOptionGroupService.save(menuService.findById(menuId), optionGroup);
        }
        return "redirect:/" + store.getSerialNumber() + "/admin/store/menuList";

    }

    /* 메뉴 삭제 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/menu/delete/{menuId}")
    public String delete(@PathVariable Long menuId) {
        Menu menu = menuService.findById(menuId);
        Store store = menu.getStore();
        menuAndOptionGroupService.deleteByMenu(menu);
        menuService.deleteByMenu(menu);

        return "redirect:/" + store.getSerialNumber() + "/admin/store/menuList";
    }

    /* 메뉴 수정 시 메뉴에 속한 옵션 그룹 삭제 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/menu/deleteOptionGroup/{menuId}/{optionGroupId}")
    public String deleteOptionGroup(@PathVariable("storeSN") String storeSN,  @PathVariable Long menuId, @PathVariable Long optionGroupId) {
        Store store = storeService.findBySerialNumber(storeSN);
        Menu menu = menuService.findById(menuId);
        OptionGroup optionGroup = optionGroupService.findByIdAndStore(optionGroupId, store);

        menuAndOptionGroupService.deleteByMenuAndOptionGroup(menu, optionGroup);
        return "redirect:/" + store.getSerialNumber() + "/admin/store/menu/modify/" + menu.getId();
    }
}