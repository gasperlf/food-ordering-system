package net.ontopsolutions.food.ordering.system.domain;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderCommand;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.mapper.OrderDataMapper;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.CustomerRepository;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.OrderRepository;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.RestaurantRepository;
import net.ontopsolutions.food.ordering.system.order.service.domain.OrderDomainService;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Customer;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Order;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Restaurant;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import net.ontopsolutions.food.ordering.system.order.service.domain.exception.OrderDomainException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class OrderCreateCommandHandler {

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDataMapper orderDataMapper;

    public OrderCreateCommandHandler(OrderDomainService orderDomainService,
                                     OrderRepository orderRepository,
                                     CustomerRepository customerRepository,
                                     RestaurantRepository restaurantRepository,
                                     OrderDataMapper orderDataMapper) {

        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        checkCustomer(createOrderCommand.getCustomerId());
        Restaurant restaurant = checkRestaurant(createOrderCommand);
        Order order = orderDataMapper.mapCreateOrderCommandAsOrder(createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);
        Order orderSaved = saveOrder(order);
        log.info("Order created: {}", orderSaved.getId().getValue());
        return orderDataMapper.mapOrderAsCreateOrderResponse(orderSaved);
    }

    private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
        Restaurant restaurant = orderDataMapper.mapCreateOrderCommandAsRestaurant(createOrderCommand);
        Optional<Restaurant> byRestaurantInformation = restaurantRepository.findByRestaurantInformation(restaurant);
        if (byRestaurantInformation.isEmpty()) {
            log.warn("Could not find restaurant information for restaurant {}", restaurant);
            throw new OrderDomainException("Could not find restaurant information for restaurant " + restaurant);
        }
        return byRestaurantInformation.get();
    }

    private void checkCustomer(@NotNull UUID customerId) {
        Optional<Customer> customer = customerRepository.findCustomer(customerId);
        if(customer.isEmpty()) {
            log.warn("Customer with id {} not found", customerId);
            throw new OrderDomainException("Customer with id " + customerId + " not found");
        }
    }

    private Order saveOrder(Order order) {
        Order orderSaved = orderRepository.save(order);
        if(orderSaved == null){
            log.error("Could not save order {}", order);
            throw new OrderDomainException("Could not save order");
        }
        log.info("Saved order id {}", orderSaved.getId().getValue());
        return orderSaved;
    }
}
