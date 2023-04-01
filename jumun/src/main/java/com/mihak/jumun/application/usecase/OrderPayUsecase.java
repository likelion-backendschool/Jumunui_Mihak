package com.mihak.jumun.application.usecase;

import com.mihak.jumun.cart.dto.CartDto;
import com.mihak.jumun.cart.service.CartService;
import com.mihak.jumun.menu.service.MenuStockService;
import com.mihak.jumun.order.dto.OrderDtoFromCart;
import com.mihak.jumun.order.dto.OrderFormDto;
import com.mihak.jumun.order.entity.Order;
import com.mihak.jumun.order.service.OrderService;
import com.mihak.jumun.pay.service.PayFactory;
import com.mihak.jumun.pay.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderPayUsecase {

    private final OrderService orderService;
    private final PayFactory payFactory;
    private final CartService cartService;
    private final MenuStockService menuStockService;

    @Transactional
    public String order(OrderDtoFromCart orderDtoFromCart, OrderFormDto orderFormDto) {
        Order order = orderService.save(orderDtoFromCart, orderFormDto);

        List<CartDto> cartDtos = cartService.getCartListByNickname(orderDtoFromCart.getUserNickname())
                .getCartDtos()
                .stream()
                .filter(m -> m.getMenu().getIsLimitedSale())
                .toList();

        cartDtos.forEach(this::decreaseQuantity);

        PayService payService = payFactory.find(order.getPayType());
        return payService.pay(order.getId());
    }

    private void decreaseQuantity(CartDto cartDto) {
        menuStockService.decreaseQuantity(cartDto.getMenu(), (long) cartDto.getCount());
    }
}
