package net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.adapter;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.OrderRepository;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.repository.OrderJpaRepository;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Order;
import net.ontopsolutions.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, OrderDataAccessMapper orderDataAccessMapper) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderDataAccessMapper = orderDataAccessMapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity orderEntity = orderDataAccessMapper.mapOrderAsOrderEntity(order);
        OrderEntity orderEntitySaved = orderJpaRepository.save(orderEntity);
        return orderDataAccessMapper.mapOrderEntityAsOrder(orderEntitySaved);
    }

    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        Optional<OrderEntity> orderEntity = orderJpaRepository.findByTrackingId(trackingId.getValue());
        return orderEntity.map(orderDataAccessMapper::mapOrderEntityAsOrder);
    }
}
