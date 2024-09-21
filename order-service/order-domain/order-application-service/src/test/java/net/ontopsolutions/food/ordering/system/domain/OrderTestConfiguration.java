package net.ontopsolutions.food.ordering.system.domain;

import net.ontopsolutions.food.ordering.system.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import net.ontopsolutions.food.ordering.system.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import net.ontopsolutions.food.ordering.system.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.CustomerRepository;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.OrderRepository;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.RestaurantRepository;
import net.ontopsolutions.food.ordering.system.order.service.domain.OrderDomainService;
import net.ontopsolutions.food.ordering.system.order.service.domain.OrderDomainServiceImpl;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "net.ontopsolutions.food.ordering.system")
public class OrderTestConfiguration {

    @Bean
    public OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher() {
        return Mockito.mock(OrderCreatedPaymentRequestMessagePublisher.class);
    }

    @Bean
    public OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher() {
        return Mockito.mock(OrderCancelledPaymentRequestMessagePublisher.class);
    }

    @Bean
    public OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher() {
        return Mockito.mock(OrderPaidRestaurantRequestMessagePublisher.class);
    }

    @Bean
    public OrderRepository orderRepository() {
        return Mockito.mock(OrderRepository.class);
    }

    @Bean
    public RestaurantRepository restaurantRepository() {
        return Mockito.mock(RestaurantRepository.class);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }

    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }

}

