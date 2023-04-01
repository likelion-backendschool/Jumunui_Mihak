package com.mihak.jumun.pay.service;

import com.mihak.jumun.pay.entity.enumuration.PayType;
import org.springframework.stereotype.Service;

@Service
public class CashPayService implements PayService {

    @Override
    public boolean supports(final PayType payType) {
        return payType == PayType.CASH;
    }

    @Override
    public String pay(Long orderId) {
        return "/cashPaySuccess/" + orderId;
    }
}
