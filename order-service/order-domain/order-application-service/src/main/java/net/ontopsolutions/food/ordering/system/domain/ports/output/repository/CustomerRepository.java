package net.ontopsolutions.food.ordering.system.domain.ports.output.repository;

import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Optional<Customer> findCustomer(UUID customerId);
}
