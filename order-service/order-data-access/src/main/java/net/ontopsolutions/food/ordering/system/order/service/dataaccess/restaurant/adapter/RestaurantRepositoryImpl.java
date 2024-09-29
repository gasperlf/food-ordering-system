package net.ontopsolutions.food.ordering.system.order.service.dataaccess.restaurant.adapter;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.ports.output.repository.RestaurantRepository;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.restaurant.repository.RestaurantJpaRepository;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;


    public RestaurantRepositoryImpl(RestaurantJpaRepository restaurantJpaRepository, RestaurantDataAccessMapper restaurantDataAccessMapper) {
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    }

    @Override
    public Optional<Restaurant> findByRestaurantInformation(Restaurant restaurant) {

        List<UUID> productIds = restaurantDataAccessMapper.mapRestaurantAsRestaurantProductIds(restaurant);
        Optional<List<RestaurantEntity>> byRestaurantIdAndProductIdIn = restaurantJpaRepository.findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(),
                productIds);
        return byRestaurantIdAndProductIdIn.map(restaurantDataAccessMapper::mapRestaurantEntityListAsRestaurant);
    }
}
