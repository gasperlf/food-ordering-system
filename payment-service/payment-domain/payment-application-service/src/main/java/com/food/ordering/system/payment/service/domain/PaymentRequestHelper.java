package com.food.ordering.system.payment.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.valueobject.CustomerId;
import com.food.ordering.system.order.service.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
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
    private final OrderOutboxHelper orderOutboxHelper;
    private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

    @Transactional
    public void persistPayment(PaymentRequest paymentRequest) {

        if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.COMPLETED)) {
            log.info(
                    "An outbox message with saga id: {} is already saved to database",
                    paymentRequest.getSagaId());
            return;
        }

        log.info("Received Payment completed event for order id: {}", paymentRequest.getOrderId());

        Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());

        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent =
                paymentDomainService.validateAndInitialPayment(
                        payment, creditEntry, creditHistories, failureMessages);
        persistPaymentDbObject(payment, failureMessages, creditEntry, creditHistories);

        orderOutboxHelper.saveOrderOutboxMessage(
                paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
                paymentEvent.getPayment().getPaymentStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(paymentRequest.getSagaId()));
    }

    @Transactional
    public void persistCancelPayment(PaymentRequest paymentRequest) {

        if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.CANCELLED)) {
            log.info(
                    "An outbox message with saga id: {} is already saved to database",
                    paymentRequest.getSagaId());
            return;
        }

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
                        payment.get(), creditEntry, creditHistories, failureMessages);

        persistPaymentDbObject(payment.get(), failureMessages, creditEntry, creditHistories);

        orderOutboxHelper.saveOrderOutboxMessage(
                paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
                paymentEvent.getPayment().getPaymentStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(paymentRequest.getSagaId()));
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

    private boolean publishIfOutboxMessageProcessedForPayment(
            PaymentRequest paymentRequest, PaymentStatus paymentStatus) {
        Optional<OrderOutboxMessage> orderOutboxMessage =
                orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(
                        UUID.fromString(paymentRequest.getSagaId()), paymentStatus);
        if (orderOutboxMessage.isPresent()) {
            paymentResponseMessagePublisher.publish(
                    orderOutboxMessage.get(), orderOutboxHelper::updateOutboxMessage);
            return true;
        }
        return false;
    }
}
