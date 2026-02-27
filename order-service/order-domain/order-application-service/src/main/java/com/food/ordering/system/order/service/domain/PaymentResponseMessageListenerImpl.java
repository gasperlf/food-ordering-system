package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.ports.input.listener.payment.PaymentResponseMessageListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

    // private final

    @Override
    public void paymentComplete(PaymentResponse paymentResponse) {}

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {}
}
