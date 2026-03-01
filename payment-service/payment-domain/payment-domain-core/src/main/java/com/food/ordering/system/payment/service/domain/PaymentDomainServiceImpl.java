package com.food.ordering.system.payment.service.domain;

import static com.food.ordering.system.order.service.domain.DomainConstants.ZONE_UTC;
import static com.food.ordering.system.payment.service.domain.valueobject.TransactionType.CREDIT;
import static com.food.ordering.system.payment.service.domain.valueobject.TransactionType.DEBIT;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.food.ordering.system.order.service.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.valueobject.Money;
import com.food.ordering.system.order.service.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.valueobject.CreditHistoryId;
import com.food.ordering.system.payment.service.domain.valueobject.TransactionType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Override
    public PaymentEvent validateAndInitialPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages,
            DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher,
            DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, DEBIT);
        validateCreditHistory(creditEntry, creditHistories, failureMessages);

        if (failureMessages.isEmpty()) {
            log.info("Payment is initiated for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(
                    payment,
                    ZonedDateTime.now(ZONE_UTC),
                    paymentCompletedEventDomainEventPublisher);
        } else {
            log.info(
                    "Payment initiation is failed for order id: {}",
                    payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(
                    payment,
                    ZonedDateTime.now(ZONE_UTC),
                    failureMessages,
                    paymentFailedEventDomainEventPublisher);
        }
    }

    @Override
    public PaymentEvent validateAndCancelPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages,
            DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher,
            DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher) {
        payment.validatePayment(failureMessages);
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, CREDIT);
        if (failureMessages.isEmpty()) {
            log.info("Payment is cancelled for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(
                    payment,
                    ZonedDateTime.now(ZONE_UTC),
                    paymentCancelledEventDomainEventPublisher);
        } else {
            log.info(
                    "Payment cancellation is failed for order id: {}",
                    payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(
                    payment,
                    ZonedDateTime.now(ZONE_UTC),
                    failureMessages,
                    paymentFailedEventDomainEventPublisher);
        }
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }

    private void validateCreditHistory(
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages) {

        Money totalCreditHistory = getTotalHistoryAmount(creditHistories, CREDIT);
        Money totalDebitHistory = getTotalHistoryAmount(creditHistories, DEBIT);

        if (totalDebitHistory.isGraterThan(totalCreditHistory)) {
            String msg =
                    String.format(
                            "Customer with id:%s does not have enough credit according to credit"
                                    + " history",
                            creditEntry.getCustomerId().getValue().toString());
            log.error(msg);
            failureMessages.add(msg);
        }

        if (!creditEntry
                .getTotalCreditAmount()
                .equals(totalCreditHistory.subtract(totalDebitHistory))) {
            String msg =
                    String.format(
                            "Credit history total is not equal to current credit for customer id:"
                                    + " %s!",
                            creditEntry.getCustomerId().getValue().toString());
            log.error(msg);
            failureMessages.add(msg);
        }
    }

    private static Money getTotalHistoryAmount(
            List<CreditHistory> creditHistories, TransactionType transactionType) {
        Predicate<CreditHistory> creditHistoryPredicate =
                ch -> ch.getTransactionType().equals(transactionType);
        return creditHistories.stream()
                .filter(creditHistoryPredicate)
                .map(CreditHistory::getAmount)
                .reduce(Money.ZERO, Money::add);
    }

    private void updateCreditHistory(
            Payment payment, List<CreditHistory> creditHistories, TransactionType transactionType) {
        creditHistories.add(
                CreditHistory.builder()
                        .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                        .customerId(payment.getCustomerId())
                        .amount(payment.getPrice())
                        .transactionType(transactionType)
                        .build());
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void validateCreditEntry(
            Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if (payment.getPrice().isGraterThan(creditEntry.getTotalCreditAmount())) {
            String msg =
                    String.format(
                            "Customer with id:%s does not have enough credit to make payment",
                            payment.getCustomerId().getValue().toString());
            log.error(msg);
            failureMessages.add(msg);
        }
    }
}
