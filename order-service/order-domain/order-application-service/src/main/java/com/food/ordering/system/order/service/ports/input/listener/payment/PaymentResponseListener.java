package com.food.ordering.system.order.service.ports.input.listener.payment;

import com.food.ordering.system.order.service.dto.message.PaymentResponse;

public interface PaymentResponseListener {

    void paymentComplete(PaymentResponse paymentResponse);

    void paymentCancelled(PaymentResponse paymentResponse);
}
