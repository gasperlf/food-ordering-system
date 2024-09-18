package net.ontopsolutions.food.ordering.system.domain.ports.output.repository;

import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {

    Optional<Restaurant> findByRestaurantInformation(Restaurant restaurant);

}
