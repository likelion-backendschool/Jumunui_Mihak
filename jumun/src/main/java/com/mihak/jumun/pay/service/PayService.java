package com.mihak.jumun.pay.service;

import com.mihak.jumun.pay.entity.enumuration.PayType;

public interface PayService {
    boolean supports(PayType payType);

    String pay(Long orderId);
}
