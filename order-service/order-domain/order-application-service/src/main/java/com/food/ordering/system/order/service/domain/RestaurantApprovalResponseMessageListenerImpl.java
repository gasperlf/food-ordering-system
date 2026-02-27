package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.ports.input.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApprovalResponseMessageListenerImpl
        implements RestaurantApprovalResponseMessageListener {

    @Override
    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {}

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {}
}
