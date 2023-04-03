package com.mihak.jumun.order.service;

import com.mihak.jumun.cart.service.CartService;
import com.mihak.jumun.cart.dto.CartDto;

import com.mihak.jumun.cart.dto.CartListDto;
import com.mihak.jumun.menu.service.MenuStockService;
import com.mihak.jumun.order.entity.Order;
import com.mihak.jumun.order.entity.enumuration.OrderStatus;
import com.mihak.jumun.pay.entity.enumuration.PayStatus;
import com.mihak.jumun.order.exception.OrderNotFoundException;
import com.mihak.jumun.order.dto.OrderDtoFromCart;
import com.mihak.jumun.order.dto.OrderFormDto;

import com.mihak.jumun.order.repository.OrderRepository;
import com.mihak.jumun.store.dto.FindByUserDailyDto;
import com.mihak.jumun.store.dto.FindListFormDto;
import com.mihak.jumun.pay.dto.PaySuccessDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MenuStockService menuStockService;

    public Order save(OrderDtoFromCart orderDtoFromCart, OrderFormDto orderFormDto) {

        Order order = Order.builder()
                .userNickname(orderDtoFromCart.getUserNickname())
                .storeSerialNumber(orderDtoFromCart.getStoreSerialNumber())
                .totalPrice(orderDtoFromCart.getTotalPrice())
                .orderType(orderDtoFromCart.getOrderType())
                .orderedAt(LocalDateTime.now())
                .payType(orderFormDto.getPayType())
                .requests(orderFormDto.getRequests())
                .payStatus(PayStatus.CONTINUE)
                .orderStatus(OrderStatus.BEFOREORDER)
                .build();

        order.setCreatedDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order findById(Long id) {
        Optional<Order> findOrder = orderRepository.findById(id);

        if (findOrder.isEmpty()) {
            throw new OrderNotFoundException("해당 주문을 찾을 수 없습니다.");
        }

        return findOrder.get();
    }

    public CartListDto getCartListDto(String userNickname) {
        return cartService.getCartListForOrder(userNickname);
    }

    @Transactional
    public void cancelOrderByPayFail(Long orderId) {
        Order order = findById(orderId);

        order.setOrderStatus(OrderStatus.CANCEL);
        order.setPayStatus(PayStatus.REFUSE);
    }

    @Transactional
    public void cancelOrderByUser(Long orderId) {
        Order order = findById(orderId);

        order.setOrderStatus(OrderStatus.CANCEL);
        order.setPayStatus(PayStatus.REFUND);

        cartService.cancelOrder(order.getUserNickname());
        increaseQuantity(order.getUserNickname());
    }

    private void increaseQuantity(String userNickname) {

        List<CartDto> cartDtos = cartService.getCartListByNickname(userNickname)
                .getCartDtos()
                .stream()
                .filter(m -> m.getMenu().getIsLimitedSale())
                .toList();

        cartDtos.forEach((cartDto) -> menuStockService.increaseQuantity(cartDto.getMenu(), (long) cartDto.getCount()));
    }

    public PaySuccessDto getPaySuccessDto(Order order) {

        List<CartDto> orderHistory = cartService.getCartDtoListByNickname(order.getUserNickname(), true);

        return PaySuccessDto.builder()
                .userNickName(order.getUserNickname())
                .orderId(order.getId())
                .orderHistory(orderHistory)
                .orderStatus(order.getOrderStatus())
                .orderTotalPrice(order.getTotalPrice())
                .storeSN(order.getStoreSerialNumber())
                .build();
    }

    public List<Order> findAllByStoreSN(String storeSN) {
        return orderRepository.findAllByStoreSerialNumber(storeSN);
    }
    
    public List<Order> findAllOrderByStoreSN(String storeSN) {
        return orderRepository.findAllByStoreSerialNumberOrderByOrderedAtAsc(storeSN);
    }

    public List<FindListFormDto> getFindListFormDtoListForPriceDaily(String storeSN) {
        List<FindListFormDto> findList = orderRepository.findByPriceDaily(storeSN);
        return findList;
    }

    public List<FindByUserDailyDto> getFindByUserDailyDtoListForUserDaily(String storeSN) {

        List<FindByUserDailyDto> findList = orderRepository.findByUserDaily(storeSN);
        return findList;
    }

// 같은 날짜(key)를 갖는다면 value를 합산
    public Map<String, Long> calculateSumForDay(List<FindListFormDto> list) {
        return list.stream().collect(Collectors.toMap(e -> e.calculateOrderedAtDaily(), e -> e.calculateTotalPrice(), Long::sum));
    }

    public Map<String, Long> calculateSumForUser(List<FindByUserDailyDto> list) {
        return list.stream().collect(Collectors.toMap(e -> e.calculateOrderedAtDaily(), e -> e.findByNickname(), Long::sum));

    }

    @Transactional
    public void changeOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = findById(orderId);
        order.setOrderStatus(orderStatus);
    }
}
