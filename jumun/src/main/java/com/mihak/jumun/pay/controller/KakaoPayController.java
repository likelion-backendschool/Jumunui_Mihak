package com.mihak.jumun.pay.controller;

import com.mihak.jumun.cart.service.CartService;
import com.mihak.jumun.order.dao.OrderHashMapCache;
import com.mihak.jumun.order.entity.Order;
import com.mihak.jumun.order.service.OrderService;
import com.mihak.jumun.pay.service.KaKaoPayService;
import com.mihak.jumun.pay.dto.PaySuccessDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class KakaoPayController {

    private final KaKaoPayService kaKaoPayService;
    private final OrderService orderService;
    private final CartService cartService;
    private final OrderHashMapCache orderHashMapCache;

    @GetMapping("/kakaopay/{orderId}")
    public String doKakaoPay(@PathVariable Long orderId) {

        return "redirect:" + kaKaoPayService.pay(orderId);
    }

    @GetMapping("/kakaoPaySuccess/{orderId}")
    public String showKakaoPayHistory(@RequestParam("pg_token") String pg_token, Model model,
                                      @PathVariable Long orderId) {

        Order order = orderService.findById(orderId);
        cartService.modifyIsOrdered(order);

        kaKaoPayService.approveKakaoPay(pg_token, orderId);
        PaySuccessDto paySuccessDto = orderService.getPaySuccessDto(order);
        orderHashMapCache.removeOrderDtoFromCart(paySuccessDto.getUserNickName());

        model.addAttribute("paySuccessDto", paySuccessDto);
        return "pay/kakaoPaySuccess";
    }

    @GetMapping("/{storeSN}/kakaopay/cancel/{orderId}")
    public String cancelKakaoPay(@PathVariable String storeSN, @PathVariable Long orderId) {

        kaKaoPayService.cancelKakaoPay(orderId);
        orderService.cancelOrderByUser(orderId);
        return "redirect:/%s/cart".formatted(storeSN);
    }

    @GetMapping("/kakaopay/status/{orderId}")
    public String getOrderStatus(@PathVariable Long orderId, Model model) {

        Order order = orderService.findById(orderId);
        model.addAttribute("userNickName", order.getUserNickname());
        model.addAttribute("orderId", order.getId());
        model.addAttribute("orderStatus", order.getOrderStatus());
        model.addAttribute("storeSN", order.getStoreSerialNumber());
        return "pay/orderStatusByKakaoPay";
    }


    /**
     *  사용하지 않는 컨트롤러 - 카카오페이 실패 로직 추가 후 사용할 예정
    */
    @GetMapping("/kakaoPayCancel/{orderId}")
    public String kakaoPayCancelFail(@RequestParam("pg_token") String pg_token, Model model,
                                 @PathVariable Long orderId) {

        orderService.cancelOrderByUser(orderId);
        model.addAttribute("info", kaKaoPayService.approveKakaoPay(pg_token, orderId));
        return "pay/kakaoPayFail";
    }

    /**
     *  사용하지 않는 컨트롤러 - 카카오페이 실패 로직 추가 후 사용할 예정
     */
    @GetMapping("/kakaoPaySuccessFail/{orderId}")
    public String kakaoPaySuccessFail(@RequestParam("pg_token") String pg_token, Model model,
                                  @PathVariable Long orderId) {

        orderService.cancelOrderByPayFail(orderId);
        model.addAttribute("info", kaKaoPayService.approveKakaoPay(pg_token, orderId));
        return "pay/kakaoPayFail";
    }
}
