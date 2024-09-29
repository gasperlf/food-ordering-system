package net.ontopsolutions.food.ordering.system.order.service.dataaccess.customer.adapter;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.CustomerRepository;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.OrderRepository;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.customer.mapper.CustomerDataAccessMapper;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.customer.repository.CustomerJpaRepository;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.repository.OrderJpaRepository;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Customer;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Order;
import net.ontopsolutions.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerDataAccessMapper customerDataAccessMapper;

    public CustomerRepositoryImpl(CustomerJpaRepository customerJpaRepository,
                                  CustomerDataAccessMapper customerDataAccessMapper) {
        this.customerJpaRepository = customerJpaRepository;
        this.customerDataAccessMapper = customerDataAccessMapper;
    }

    @Override
    public Optional<Customer> findCustomer(UUID customerId) {
        return customerJpaRepository.findById(customerId)
                .map(customerDataAccessMapper::mapCustomerEntityAsCustomer);
    }
}
