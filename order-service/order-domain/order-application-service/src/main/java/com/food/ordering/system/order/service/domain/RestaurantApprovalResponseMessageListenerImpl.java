package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

import org.springframework.stereotype.Service;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.input.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApprovalResponseMessageListenerImpl
        implements RestaurantApprovalResponseMessageListener {

    private final OrderApprovalSaga orderApprovalSaga;

    @Override
    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {
        orderApprovalSaga.process(restaurantApprovalResponse);
    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
        OrderCancelledEvent rollback = orderApprovalSaga.rollback(restaurantApprovalResponse);
        log.info(
                "Publishing order cancelled event dor order id {} with failure messages {}",
                restaurantApprovalResponse.getOrderId(),
                String.join(
                        FAILURE_MESSAGE_DELIMITER,
                        restaurantApprovalResponse.getFailureMessages()));
        rollback.fire();
    }
}
