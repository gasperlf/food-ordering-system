package net.ontopsolutions.food.ordering.system.domain;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.dto.track.TrackOrderQuery;
import net.ontopsolutions.food.ordering.system.domain.dto.track.TrackOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.mapper.OrderDataMapper;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.OrderRepository;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Order;
import net.ontopsolutions.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import net.ontopsolutions.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Slf4j
public class OrderTrackCommandHandler {

   private final OrderDataMapper orderDataMapper;
   private final OrderRepository orderRepository;

   public OrderTrackCommandHandler(OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
      this.orderDataMapper = orderDataMapper;
      this.orderRepository = orderRepository;
   }

   @Transactional(readOnly = true)
   public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
      Optional<Order> orderOptional = orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));
      if (orderOptional.isEmpty()) {
         log.warn("Could not find order with trackingId: {}", trackOrderQuery.getOrderTrackingId());
         throw new OrderNotFoundException("Could not find order with trackingId:"  + trackOrderQuery.getOrderTrackingId());
      }

      return orderDataMapper.mapOrderAsTrackOrderResponse(orderOptional.get());
   }
}
