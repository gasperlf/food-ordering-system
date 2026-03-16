package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.order.service.domain.DomainConstants.ZONE_UTC;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.exception.DomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.order.service.domain.valueobject.OrderStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper orderDataMapper;

    @Override
    @Transactional
    public void process(RestaurantApprovalResponse restaurantApprovalResponse) {

        Optional<OrderApprovalOutboxMessage> approvalOutboxMessageBySagaIdAndSagaStatus =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(restaurantApprovalResponse.getSagaId()),
                        SagaStatus.PROCESSING);

        if (approvalOutboxMessageBySagaIdAndSagaStatus.isEmpty()) {
            log.info(
                    "An outbox message with saga id {} is already processed",
                    restaurantApprovalResponse.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage orderApprovalOutboxMessage =
                approvalOutboxMessageBySagaIdAndSagaStatus.get();

        Order order = approveOrder(restaurantApprovalResponse);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

        approvalOutboxHelper.save(
                getUpdatedApprovalOutboxMessage(
                        orderApprovalOutboxMessage, order.getOrderStatus(), sagaStatus));

        paymentOutboxHelper.save(
                getUpdatedPaymentOutboxMessage(
                        restaurantApprovalResponse.getSagaId(),
                        order.getOrderStatus(),
                        sagaStatus));

        log.info("Order is approved for order id {}", restaurantApprovalResponse.getOrderId());
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(
            String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {

        Optional<OrderPaymentOutboxMessage> paymentOutboxMessageBySagaIdAndSagaStatus =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(sagaId), SagaStatus.PROCESSING);

        if (paymentOutboxMessageBySagaIdAndSagaStatus.isEmpty()) {
            throw new DomainException(
                    "Payment outbox message with saga id "
                            + sagaId
                            + " can not be found "
                            + SagaStatus.PROCESSING.name()
                            + " state");
        }

        OrderPaymentOutboxMessage orderPaymentOutboxMessage =
                paymentOutboxMessageBySagaIdAndSagaStatus.get();
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZONE_UTC));

        return orderPaymentOutboxMessage;
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(
            OrderApprovalOutboxMessage orderApprovalOutboxMessage,
            OrderStatus orderStatus,
            SagaStatus sagaStatus) {
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZONE_UTC));
        return orderApprovalOutboxMessage;
    }

    private Order approveOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
        log.info(
                "Processing order approval saga for order id {}",
                restaurantApprovalResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
        orderDomainService.approveOrder(order);
        orderSagaHelper.saveOrder(order);
        return order;
    }

    @Override
    @Transactional
    public void rollback(RestaurantApprovalResponse restaurantApprovalResponse) {

        Optional<OrderApprovalOutboxMessage> approvalOutboxMessageBySagaIdAndSagaStatus =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(restaurantApprovalResponse.getSagaId()),
                        SagaStatus.PROCESSING);

        if (approvalOutboxMessageBySagaIdAndSagaStatus.isEmpty()) {
            log.info(
                    "An outbox message with saga id {} is already processed",
                    restaurantApprovalResponse.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage orderApprovalOutboxMessage =
                approvalOutboxMessageBySagaIdAndSagaStatus.get();

        OrderCancelledEvent domainEvent = rollbackOrder(restaurantApprovalResponse);

        SagaStatus sagaStatus =
                orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder().getOrderStatus());

        approvalOutboxHelper.save(
                getUpdatedApprovalOutboxMessage(
                        orderApprovalOutboxMessage,
                        domainEvent.getOrder().getOrderStatus(),
                        sagaStatus));

        paymentOutboxHelper.savePaymentOutboxMessage(
                orderDataMapper.orderCancelledEventToOrderPaymentEventPayload(domainEvent),
                domainEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(restaurantApprovalResponse.getOrderId()));

        log.info("Order is cancelled for order id {}", domainEvent.getOrder().getId().getValue());
    }

    private OrderCancelledEvent rollbackOrder(
            RestaurantApprovalResponse restaurantApprovalResponse) {
        log.info("Cancelling order with order id {}", restaurantApprovalResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
        OrderCancelledEvent orderCancelledEvent =
                orderDomainService.cancelOrderPayment(
                        order, restaurantApprovalResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        return orderCancelledEvent;
    }
}
