package com.food.ordering.system.payment.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentFailedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestHelper {

    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentCompletedMessagePublisher paymentCompletedEventDomainEventPublisher;
    private final PaymentCancelledMessagePublisher paymentCancelledEventDomainEventPublisher;
    private final PaymentFailedMessagePublisher paymentFailedEventDomainEventPublisher;

    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Received Payment completed event for order id: {}", paymentRequest.getOrderId());

        Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());

        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent =
                paymentDomainService.validateAndInitialPayment(
                        payment,
                        creditEntry,
                        creditHistories,
                        failureMessages,
                        paymentCompletedEventDomainEventPublisher,
                        paymentFailedEventDomainEventPublisher);
        persistPaymentDbObject(payment, failureMessages, creditEntry, creditHistories);

        return paymentEvent;
    }

    private void persistPaymentDbObject(
            Payment payment,
            List<String> failureMessages,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories) {
        paymentRepository.save(payment);

        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistories.getLast());
        }
    }

    @Transactional
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
        log.info("Received Payment rollback event for order id: {}", paymentRequest.getOrderId());
        Optional<Payment> payment =
                paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));

        if (payment.isEmpty()) {
            log.info("Payment not found for order id: {}", paymentRequest.getOrderId());
            throw new PaymentApplicationServiceException(
                    "Payment not found for order id: " + paymentRequest.getOrderId());
        }
        CreditEntry creditEntry = getCreditEntry(payment.get().getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.get().getCustomerId());

        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent =
                paymentDomainService.validateAndCancelPayment(
                        payment.get(),
                        creditEntry,
                        creditHistories,
                        failureMessages,
                        paymentCancelledEventDomainEventPublisher,
                        paymentFailedEventDomainEventPublisher);

        persistPaymentDbObject(payment.get(), failureMessages, creditEntry, creditHistories);

        return paymentEvent;
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId) {
        Optional<List<CreditHistory>> creditHistories =
                creditHistoryRepository.findByCustomerId(customerId);

        if (creditHistories.isEmpty()) {
            String msg =
                    String.format(
                            "Could not find credit history for customerId: %s",
                            customerId.getValue());
            log.error(msg);
            throw new PaymentApplicationServiceException(msg);
        }
        return creditHistories.get();
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        Optional<CreditEntry> creditEntryOptional =
                creditEntryRepository.findByCustomerId(customerId);
        if (creditEntryOptional.isEmpty()) {
            String msg =
                    String.format(
                            "Could not find credit entry for customer id: %s",
                            customerId.getValue());
            log.error(msg);
            throw new PaymentApplicationServiceException(msg);
        }
        return creditEntryOptional.get();
    }
}
