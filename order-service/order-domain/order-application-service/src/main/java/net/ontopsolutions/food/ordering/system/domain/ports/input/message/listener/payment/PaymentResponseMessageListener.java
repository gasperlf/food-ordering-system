package net.ontopsolutions.food.ordering.system.domain.ports.input.message.listener.payment;

import net.ontopsolutions.food.ordering.system.domain.dto.message.PaymentResponse;

public interface PaymentResponseMessageListener {

    void paymentCompleted(PaymentResponse paymentResponse);
    void paymentCancelled(PaymentResponse paymentResponse);
}
