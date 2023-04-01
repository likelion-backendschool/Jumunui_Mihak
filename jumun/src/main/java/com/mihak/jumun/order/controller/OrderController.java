package com.mihak.jumun.order.controller;

import com.mihak.jumun.application.usecase.OrderPayUsecase;
import com.mihak.jumun.order.dao.OrderHashMapCache;
import com.mihak.jumun.pay.entity.enumuration.PayType;
import com.mihak.jumun.store.entity.Store;
import com.mihak.jumun.order.dto.OrderDtoFromCart;
import com.mihak.jumun.order.dto.OrderFormDto;
import com.mihak.jumun.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final StoreService storeService;
    private final OrderHashMapCache orderHashMapCache;
    private final OrderPayUsecase orderPayUsecase;

    @PostMapping("/{storeSN}/order")
    public String createOrderDtoFormCart(@PathVariable String storeSN,
                        HttpServletRequest request, @CookieValue("customerLogin") String customerKey,
                        @ModelAttribute OrderDtoFromCart orderDtoFromCart) {
        HttpSession session = request.getSession(true);
        String userNickname = session.getAttribute(customerKey).toString();

        orderDtoFromCart.setUserNickname(userNickname);
        orderDtoFromCart.setStoreSerialNumber(storeSN);

        orderHashMapCache.putOrderDtoFromCart(orderDtoFromCart, userNickname);
        return "redirect:/" + storeSN + "/pay";
    }

    @ModelAttribute("payTypes")
    public PayType[] payTypes() {
        return PayType.values();
    }

    @GetMapping("/{storeSN}/pay")
    public String showOrder(@PathVariable String storeSN,
                            HttpServletRequest request, @CookieValue("customerLogin") String customerKey,
                            Model model) {

        HttpSession session = request.getSession(true);
        String userNickname = session.getAttribute(customerKey).toString();
        Store store = storeService.findBySerialNumber(storeSN);

        OrderDtoFromCart dto = orderHashMapCache.getOrderDtoFromCart(userNickname);

        model.addAttribute("orderFormDto", new OrderFormDto());
        model.addAttribute("storeName", store.getName());
        model.addAttribute("totalPrice", dto.getTotalPrice());

        return "order/orderForm";
    }

    @PostMapping("/{storeSN}/pay")
    public String doOrder(@PathVariable String storeSN, HttpServletRequest request,
                          @CookieValue("customerLogin") String customerKey, @ModelAttribute OrderFormDto orderFormDto) {

        HttpSession session = request.getSession(true);
        String userNickname = session.getAttribute(customerKey).toString();

        OrderDtoFromCart orderDtoFromCart = orderHashMapCache.getOrderDtoFromCart(userNickname);

        return "redirect:" + orderPayUsecase.order(orderDtoFromCart, orderFormDto);
    }
}
