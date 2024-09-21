package net.ontopsolutions.food.ordering.system.domain;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.dto.message.RestaurantApprovalResponse;
import net.ontopsolutions.food.ordering.system.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalMessageListener {

    @Override
    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {

    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {

    }
}
