package net.ontopsolutions.food.ordering.system.domain;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.dto.message.PaymentResponse;
import net.ontopsolutions.food.ordering.system.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {


    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {

    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {

    }
}
