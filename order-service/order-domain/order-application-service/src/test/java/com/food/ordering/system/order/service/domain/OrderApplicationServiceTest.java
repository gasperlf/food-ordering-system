package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.order.service.domain.valueobject.OrderStatus.PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.order.service.domain.valueobject.CustomerId;
import com.food.ordering.system.order.service.domain.valueobject.Money;
import com.food.ordering.system.order.service.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.domain.valueobject.ProductId;
import com.food.ordering.system.order.service.domain.valueobject.RestaurantId;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {

    @Autowired OrderApplicationService orderApplicationService;

    @Autowired OrderDataMapper orderDataMapper;

    @Autowired OrderRepository orderRepository;

    @Autowired CustomerRepository customerRepository;

    @Autowired RestaurantRepository restaurantRepository;

    CreateOrderCommand createOrderCommand;
    CreateOrderCommand createOrderCommandWrongPrince;
    CreateOrderCommand createOrderCommandWrongProductPrice;

    final UUID CUSTOMER_ID = UUID.fromString("019c9fc9-2e0d-7d5e-bbb7-d918a7e04444");
    final UUID RESTAURANT_ID = UUID.fromString("019c9fc9-6317-71a0-a6dd-957b12e0c2f5");
    final UUID PRODUCT_ID = UUID.fromString("019c9fc9-6317-7f59-b98c-d250d34ebf9f");
    final UUID ORDER_ID = UUID.fromString("019c9fca-2abb-70c1-ab45-a12189b03e0a");

    final BigDecimal PRICE = BigDecimal.valueOf(200.00);

    @BeforeAll
    public void setup() {
        createOrderCommand =
                CreateOrderCommand.builder()
                        .customerId(CUSTOMER_ID)
                        .restaurantId(RESTAURANT_ID)
                        .address(
                                OrderAddress.builder()
                                        .street("street")
                                        .postalCode("1000AB")
                                        .city("Paris")
                                        .build())
                        .price(PRICE)
                        .items(
                                List.of(
                                        OrderItem.builder()
                                                .productId(PRODUCT_ID)
                                                .quantity(1)
                                                .price(BigDecimal.valueOf(50.00))
                                                .subtotal(BigDecimal.valueOf(50.00))
                                                .build(),
                                        OrderItem.builder()
                                                .productId(PRODUCT_ID)
                                                .quantity(3)
                                                .price(BigDecimal.valueOf(50.00))
                                                .subtotal(BigDecimal.valueOf(150.00))
                                                .build()))
                        .build();

        createOrderCommandWrongPrince =
                CreateOrderCommand.builder()
                        .customerId(CUSTOMER_ID)
                        .restaurantId(RESTAURANT_ID)
                        .address(
                                OrderAddress.builder()
                                        .street("street")
                                        .postalCode("1000AB")
                                        .city("Paris")
                                        .build())
                        .price(BigDecimal.valueOf(250.00))
                        .items(
                                List.of(
                                        OrderItem.builder()
                                                .productId(PRODUCT_ID)
                                                .quantity(1)
                                                .price(BigDecimal.valueOf(50.00))
                                                .subtotal(BigDecimal.valueOf(50.00))
                                                .build(),
                                        OrderItem.builder()
                                                .productId(PRODUCT_ID)
                                                .quantity(3)
                                                .price(BigDecimal.valueOf(50.00))
                                                .subtotal(BigDecimal.valueOf(150.00))
                                                .build()))
                        .build();

        createOrderCommandWrongProductPrice =
                CreateOrderCommand.builder()
                        .customerId(CUSTOMER_ID)
                        .restaurantId(RESTAURANT_ID)
                        .address(
                                OrderAddress.builder()
                                        .street("street")
                                        .postalCode("1000AB")
                                        .city("Paris")
                                        .build())
                        .price(BigDecimal.valueOf(210.00))
                        .items(
                                List.of(
                                        OrderItem.builder()
                                                .productId(PRODUCT_ID)
                                                .quantity(1)
                                                .price(BigDecimal.valueOf(60.00))
                                                .subtotal(BigDecimal.valueOf(60.00))
                                                .build(),
                                        OrderItem.builder()
                                                .productId(PRODUCT_ID)
                                                .quantity(3)
                                                .price(BigDecimal.valueOf(50.00))
                                                .subtotal(BigDecimal.valueOf(150.00))
                                                .build()))
                        .build();
    }

    @Test
    void shouldCreateOrder() {

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse =
                Restaurant.builder()
                        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                        .products(
                                List.of(
                                        new Product(
                                                new ProductId(PRODUCT_ID),
                                                "product-1",
                                                new Money(BigDecimal.valueOf(50.00))),
                                        new Product(
                                                new ProductId(PRODUCT_ID),
                                                "product-2",
                                                new Money(BigDecimal.valueOf(50.00)))))
                        .active(true)
                        .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findRestaurantInformation(any(Restaurant.class)))
                .thenReturn(Optional.of(restaurantResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
        assertEquals(PENDING, response.getOrderStatus());
        assertEquals("Order created successfully", response.getMessage());
        assertNotNull(response.getOrderTrackingId());
    }

    @Test
    void shouldCreateOrderWithWrongPrice() {

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse =
                Restaurant.builder()
                        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                        .products(
                                List.of(
                                        new Product(
                                                new ProductId(PRODUCT_ID),
                                                "product-1",
                                                new Money(BigDecimal.valueOf(50.00))),
                                        new Product(
                                                new ProductId(PRODUCT_ID),
                                                "product-2",
                                                new Money(BigDecimal.valueOf(50.00)))))
                        .active(true)
                        .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findRestaurantInformation(any(Restaurant.class)))
                .thenReturn(Optional.of(restaurantResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDomainException ex =
                assertThrows(
                        OrderDomainException.class,
                        () -> orderApplicationService.createOrder(createOrderCommandWrongPrince));
        assertEquals("Total price 250.00 is not equal to 200.00!", ex.getMessage());
    }

    @Test
    void shouldCreateOrderWithWrongProductPrice() {

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse =
                Restaurant.builder()
                        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                        .products(
                                List.of(
                                        new Product(
                                                new ProductId(PRODUCT_ID),
                                                "product-1",
                                                new Money(BigDecimal.valueOf(50.00))),
                                        new Product(
                                                new ProductId(PRODUCT_ID),
                                                "product-2",
                                                new Money(BigDecimal.valueOf(50.00)))))
                        .active(true)
                        .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findRestaurantInformation(any(Restaurant.class)))
                .thenReturn(Optional.of(restaurantResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDomainException ex =
                assertThrows(
                        OrderDomainException.class,
                        () ->
                                orderApplicationService.createOrder(
                                        createOrderCommandWrongProductPrice));
        assertEquals(
                "Order item price 60.00 is not valid for"
                        + " product019c9fc9-6317-7f59-b98c-d250d34ebf9fsubtotal 60.00",
                ex.getMessage());
    }

    @Test
    void shouldCreateOrderWithPassiveRestaurant() {

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse =
                Restaurant.builder()
                        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                        .products(
                                List.of(
                                        new Product(
                                                new ProductId(PRODUCT_ID),
                                                "product-1",
                                                new Money(BigDecimal.valueOf(50.00))),
                                        new Product(
                                                new ProductId(PRODUCT_ID),
                                                "product-2",
                                                new Money(BigDecimal.valueOf(50.00)))))
                        .active(false)
                        .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findRestaurantInformation(any(Restaurant.class)))
                .thenReturn(Optional.of(restaurantResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDomainException ex =
                assertThrows(
                        OrderDomainException.class,
                        () -> orderApplicationService.createOrder(createOrderCommand));
        assertEquals(
                "Restaurant with id 019c9fc9-6317-71a0-a6dd-957b12e0c2f5 is currently not active",
                ex.getMessage());
    }
}
