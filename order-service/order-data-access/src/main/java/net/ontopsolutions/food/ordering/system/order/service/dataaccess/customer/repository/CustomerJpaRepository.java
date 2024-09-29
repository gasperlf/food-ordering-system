package net.ontopsolutions.food.ordering.system.order.service.dataaccess.customer.repository;

import net.ontopsolutions.food.ordering.system.order.service.dataaccess.customer.entity.CustomerEntity;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
}
