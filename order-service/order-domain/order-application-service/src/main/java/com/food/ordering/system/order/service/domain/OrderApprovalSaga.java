package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.saga.SagaStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalSaga
        implements SagaStep<RestaurantApprovalResponse, EmptyEvent, OrderCancelledEvent> {

    private final OrderDomainService orderDomainService;
    private final OrderCancelledPaymentRequestMessagePublisher
            orderCancelledPaymentRequestMessagePublisher;
    private final OrderSagaHelper orderSagaHelper;

    @Override
    @Transactional
    public EmptyEvent process(RestaurantApprovalResponse data) {

        log.info("Processing order approval saga for order id {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        orderDomainService.approveOrder(order);
        orderSagaHelper.saveOrder(order);
        log.info("Order is approved for order id {}", data.getOrderId());
        return EmptyEvent.INSTANCE;
    }

    @Override
    @Transactional
    public OrderCancelledEvent rollback(RestaurantApprovalResponse data) {
        log.info("Cancelling order with order id {}", data.getOrderId());
        Order order = orderSagaHelper.findOrder(data.getOrderId());
        OrderCancelledEvent orderCancelledEvent =
                orderDomainService.cancelOrderPayment(
                        order,
                        data.getFailureMessages(),
                        orderCancelledPaymentRequestMessagePublisher);
        orderSagaHelper.saveOrder(order);
        log.info("Order is cancelled for order id {}", data.getOrderId());
        return orderCancelledEvent;
    }
}
