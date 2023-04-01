package com.mihak.jumun.pay.service;

import com.mihak.jumun.pay.entity.enumuration.PayType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PayFactory {

    private final List<PayService> payServiceList;

    public PayService find(final PayType payType) {
        return payServiceList.stream()
                .filter(v -> v.supports(payType))
                .findFirst()
                .orElseThrow();
    }
}
