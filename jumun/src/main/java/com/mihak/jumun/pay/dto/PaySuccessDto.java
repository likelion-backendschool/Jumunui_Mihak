package com.mihak.jumun.pay.dto;

import com.mihak.jumun.cart.dto.CartDto;
import com.mihak.jumun.order.entity.enumuration.OrderStatus;
import lombok.*;

import java.util.List;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaySuccessDto {
    private String userNickName;
    private Long orderId;
    private List<CartDto> orderHistory;
    private OrderStatus orderStatus;
    private int orderTotalPrice;
    private String storeSN;
}
