package net.ontopsolutions.food.ordering.system.order.service.dataaccess.customer.mapper;

import net.ontopsolutions.food.ordering.system.domain.valueobject.CustomerId;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.customer.entity.CustomerEntity;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {

    public Customer mapCustomerEntityAsCustomer(CustomerEntity customerEntity) {
        return new Customer(new CustomerId(customerEntity.getId()));
    }
}
