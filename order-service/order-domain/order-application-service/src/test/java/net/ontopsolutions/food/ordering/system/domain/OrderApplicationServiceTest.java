package net.ontopsolutions.food.ordering.system.domain;

import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderCommand;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.dto.create.OrderAddress;
import net.ontopsolutions.food.ordering.system.domain.dto.create.OrderItem;
import net.ontopsolutions.food.ordering.system.domain.mapper.OrderDataMapper;
import net.ontopsolutions.food.ordering.system.domain.ports.input.service.OrderApplicationService;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.CustomerRepository;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.OrderRepository;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.RestaurantRepository;
import net.ontopsolutions.food.ordering.system.domain.valueobject.*;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Customer;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Order;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Product;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Restaurant;
import net.ontopsolutions.food.ordering.system.order.service.domain.exception.OrderDomainException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {

    @Autowired
    OrderApplicationService orderApplicationService;

    @Autowired
    OrderDataMapper orderDataMapper;

    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    OrderRepository orderRepository;

    CreateOrderCommand createOrderCommand;
    CreateOrderCommand createOrderCommandWrongPrice;
    CreateOrderCommand createOrderCommandWrongProductPrice;

    UUID CUSTOMER_ID = UUID.fromString("88a90698-9420-42bc-9ec9-6b45c9c7e7b9");
    UUID RESTAURANT_ID = UUID.fromString("5ef44067-5d9b-4463-9f31-5b7b17523e17");
    UUID PRODUCT_ID = UUID.fromString("23378fb3-45ab-498d-ba1a-8c6931c651cb");
    UUID ORDER_ID = UUID.fromString("fc857a5d-ccab-4d3c-9033-cd8f3dbea917");

    BigDecimal PRICE = new BigDecimal("200.00");

    @BeforeAll
    public void init() {
        createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("Street name")
                        .zipCode("39004")
                        .city("San Francisco")
                        .build())
                .price(PRICE)
                .orderItems(List.of(
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        createOrderCommandWrongPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("Street name")
                        .zipCode("39004")
                        .city("San Francisco")
                        .build())
                .price(BigDecimal.valueOf(250.00))
                .orderItems(List.of(
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("Street name")
                        .zipCode("39004")
                        .city("San Francisco")
                        .build())
                .price(new BigDecimal("210.00"))
                .orderItems(List.of(
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("60.00"))
                                .subTotal(new BigDecimal("60.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();
        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));
        Restaurant restaurantResponse = Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(new Product(new ProductId(PRODUCT_ID), "Product-1", new Money(new BigDecimal("50.00"))),
                        new Product(new ProductId(PRODUCT_ID), "Product-2", new Money(new BigDecimal("50.00")))                   ))
                .active(true)
                .build();

        Order order = orderDataMapper.mapCreateOrderCommandAsOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));
        Restaurant restaurantMapper = orderDataMapper.mapCreateOrderCommandAsRestaurant(createOrderCommand);
        when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findByRestaurantInformation(any(Restaurant.class)))
                .thenReturn(Optional.of(restaurantResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
    }

    @Test
    void testCreateOrder() {
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        assertThat(createOrderResponse.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(createOrderResponse.getMessage()).isEqualTo("Order created successfully");
        assertThat(createOrderResponse.getOrderTrackingId()).isNotNull();
    }

    @Test
    void testCreateOrderWithWrongTotalPrice() {
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommandWrongPrice));

        assertThat(orderDomainException.getMessage()).isEqualTo("Total price: 250.0 is not equal to Order items total: 200.00!");
    }

    @Test
    void testCreateOrderWithWrongProductPrice() {
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));

        assertThat(orderDomainException.getMessage()).isEqualTo("Order item price: 60.00 is not valid for product: 23378fb3-45ab-498d-ba1a-8c6931c651cb");
    }

    @Test
    void testCreateOrderWithPassiveRestaurant() {
        Restaurant restaurantResponse = Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(new Product(new ProductId(PRODUCT_ID), "Product-1", new Money(new BigDecimal("50.00"))),
                        new Product(new ProductId(PRODUCT_ID), "Product-2", new Money(new BigDecimal("50.00")))                   ))
                .active(false)
                .build();
        when(restaurantRepository.findByRestaurantInformation(any(Restaurant.class)))
                .thenReturn(Optional.of(restaurantResponse));

        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));

        assertThat(orderDomainException.getMessage()).isEqualTo("Restaurant with id:5ef44067-5d9b-4463-9f31-5b7b17523e17 is currently not active");
    }
}
