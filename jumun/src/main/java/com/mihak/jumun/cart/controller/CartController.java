package com.mihak.jumun.cart.controller;

import com.mihak.jumun.cart.service.CartService;
import com.mihak.jumun.cart.dto.CartDetailDto;
import com.mihak.jumun.cart.dto.CartFormDto;
import com.mihak.jumun.cart.dto.CartListDto;
import com.mihak.jumun.cartAndOption.service.CartAndOptionService;
import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.service.MenuService;
import com.mihak.jumun.order.dto.OrderDtoFromCart;
import com.mihak.jumun.order.entity.enumuration.OrderType;
import com.mihak.jumun.store.entity.Store;
import com.mihak.jumun.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final MenuService menuService;
    private final CartAndOptionService cartAndOptionService;
    private final StoreService storeService;

    @GetMapping("/{storeSN}/cart")
    public String showCart(@PathVariable String storeSN, Model model,
                           HttpServletRequest request, @CookieValue("customerLogin") String customerKey) {

        HttpSession session = request.getSession(true);
        String userNickname = session.getAttribute(customerKey).toString();

        Store store = storeService.findBySerialNumber(storeSN);
        CartListDto cartListDto = cartService.getCartListByNickname(userNickname);

        model.addAttribute("cartListDto", cartListDto);
        model.addAttribute("storeName", store.getName());
        model.addAttribute("storeSN", storeSN);
        model.addAttribute("orderDtoFromCart", new OrderDtoFromCart());
        return "cart/cart_list";
    }

    @ModelAttribute("orderTypes")
    public OrderType[] orderTypes() {
        return OrderType.values();
    }

    @PostMapping("/{storeSN}/menu/{menuId}/option")
    public String save(@PathVariable String storeSN,
                           @PathVariable Long menuId, @ModelAttribute CartFormDto cartFormDto,
                           HttpServletRequest request, @CookieValue("customerLogin") String customerKey) {
        HttpSession session = request.getSession(true);
        String userNickname = session.getAttribute(customerKey).toString();
        Menu menu = menuService.findById(menuId);

        cartService.save(cartFormDto, userNickname, menu);
        return "redirect:/" + storeSN + "/menu";
    }

    @GetMapping("/{storeSN}/cart/detail/{cartId}")
    public String showCartDetail(@PathVariable String storeSN, @PathVariable Long cartId,
                            Model model, HttpServletRequest request,
                            @CookieValue("customerLogin") String customerKey) {
        CartDetailDto cartDetailDto = cartService.getCartDetailDtoById(cartId);
        model.addAttribute("cartDetailDto", cartDetailDto);
        return "cart/cart_detail";
    }

    @GetMapping("/{storeSN}/cart/delete/{cartId}")
    public String delete(@PathVariable String storeSN, @PathVariable Long cartId) {
        cartService.deleteCartById(cartId);
        return "redirect:/" + storeSN + "/cart";
    }

    @GetMapping("{storeSN}/cart/modify/{cartId}")
    public String showModifyForm(@PathVariable String storeSN, @PathVariable Long cartId,
                             Model model, HttpServletRequest request,
                             @CookieValue("customerLogin") String customerKey) {
        CartFormDto cartFormDto = cartService.getCartFormById(cartId);
        model.addAttribute("cartFormDto", cartFormDto);
        model.addAttribute("storeSN", storeSN);
        model.addAttribute("cardId", cartId);
        return "cart/cart_modify_form";
    }

    @PostMapping("{storeSN}/cart/modify/{cartId}")
    public String modify(@PathVariable String storeSN, @PathVariable Long cartId,
                             @ModelAttribute CartFormDto cartFormDto, HttpServletRequest request,
                             @CookieValue("customerLogin") String customerKey) {

        cartService.modify(cartId, cartFormDto);
        return "redirect:/" + storeSN + "/cart";
    }
}
