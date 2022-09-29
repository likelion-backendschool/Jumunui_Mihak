package com.mihak.jumun.pay;

import com.mihak.jumun.entity.Order;
import com.mihak.jumun.entity.PayStatus;
import com.mihak.jumun.order.OrderService;
import com.mihak.jumun.pay.response.approval.KakaoPayApproval;
import com.mihak.jumun.pay.response.KakaoPayResponse;
import com.mihak.jumun.pay.response.cancel.KakaoPayCancel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KaKaoPayService {

    private static final String HOST = "https://kapi.kakao.com";

    private KakaoPayResponse kakaoPayResponse;
    private KakaoPayApproval kakaoPayApproval;
    private final OrderService orderService;
    private KakaoPayCancel kakaoPayCancel;

    @Value("${kakao-admin-key}")
    private String kakao_admin_key;

    public String kakaoPayReady(Long orderId) {

        RestTemplate restTemplate = new RestTemplate();

        Order order = orderService.findOrderById(orderId);

        // 서버로 요청할 Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + kakao_admin_key);
        headers.add("Accept", String.valueOf(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        // 서버로 요청할 Body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("partner_order_id", String.valueOf(order.getId()));
        params.add("partner_user_id", order.getUserNickName());
        params.add("item_name", order.getUserNickName() + "님의 주문하신 음식");
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(order.getTotalPrice()));
        params.add("tax_free_amount", "0");
        params.add("approval_url", "http://34.201.102.196:8080/kakaoPaySuccess/" + orderId);
        params.add("cancel_url", "http://34.201.102.196:8080/kakaoPayCancel/" + orderId);
        params.add("fail_url", "http://34.201.102.196:8080/kakaoPaySuccessFail/" + orderId);

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        try {
            kakaoPayResponse = restTemplate.postForObject(new URI(HOST + "/v1/payment/ready"), body, KakaoPayResponse.class);
            return kakaoPayResponse.getNext_redirect_mobile_url();

        } catch (RestClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "/pay";
    }

    @Transactional
    public KakaoPayApproval kakaoPayApprove(String pg_token, Long orderId) {

        RestTemplate restTemplate = new RestTemplate();

        Order order = orderService.findOrderById(orderId);

        // 서버로 요청할 Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + kakao_admin_key);
        headers.add("Accept", String.valueOf(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        // 서버로 요청할 Body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", kakaoPayResponse.getTid());
        log.info("KakaoPayApproval tid : {}", kakaoPayResponse.getTid());
        params.add("partner_order_id", String.valueOf(order.getId()));
        params.add("partner_user_id", order.getUserNickName());
        params.add("pg_token", pg_token);
        params.add("total_amount", String.valueOf(Math.round(order.getTotalPrice())));

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        try {
            kakaoPayApproval = restTemplate.postForObject(new URI(HOST + "/v1/payment/approve"), body, KakaoPayApproval.class);
            order.setPayStatus(PayStatus.COMPLETE);
            return kakaoPayApproval;

        } catch (RestClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public KakaoPayCancel kakaoPayCancel(Long orderId) {

        RestTemplate restTemplate = new RestTemplate();

        Order order = orderService.findOrderById(orderId);

        // 서버로 요청할 Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + kakao_admin_key);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        // 서버로 요청할 Body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", kakaoPayResponse.getTid());
        log.info("KakaoPayCancel tid : {}", kakaoPayResponse.getTid());
        params.add("cancel_amount", String.valueOf(order.getTotalPrice()));
        params.add("cancel_tax_free_amount", "0");

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        try {
            kakaoPayCancel = restTemplate.postForObject(new URI(HOST + "/v1/payment/cancel"), body, KakaoPayCancel.class);
            return kakaoPayCancel;
        } catch (RestClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
