package net.ontopsolutions.food.ordering.system.domain.ports.input.message.listener.restaurantapproval;

import net.ontopsolutions.food.ordering.system.domain.dto.message.RestaurantApprovalResponse;

public interface RestaurantApprovalMessageListener {

    void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);

    void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);

}
