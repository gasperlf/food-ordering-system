package net.ontopsolutions.food.ordering.system.order.service.dataaccess.restaurant.mapper;

import net.ontopsolutions.food.ordering.system.domain.valueobject.CustomerId;
import net.ontopsolutions.food.ordering.system.domain.valueobject.Money;
import net.ontopsolutions.food.ordering.system.domain.valueobject.ProductId;
import net.ontopsolutions.food.ordering.system.domain.valueobject.RestaurantId;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.customer.entity.CustomerEntity;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.restaurant.RestaurantDataAccessException;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Customer;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Product;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RestaurantDataAccessMapper {

    public List<UUID> mapRestaurantAsRestaurantProductIds(Restaurant restaurant){
        return restaurant.getProducts().stream()
                .map(product-> product.getId().getValue())
                .toList();
    }

    public Restaurant mapRestaurantEntityListAsRestaurant(List<RestaurantEntity> restaurantEntityList) {
        RestaurantEntity restaurantEntity = restaurantEntityList.stream().findFirst()
                .orElseThrow(() -> new RestaurantDataAccessException("Restaurant could be not found!"));

        List<Product> products = restaurantEntityList.stream()
                .map(this::mapRestaurantEntityAsProduct)
                .toList();

        return Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
                .active(restaurantEntity.getRestaurantActive())
                .products(products)
                .build();
    }

    private Product mapRestaurantEntityAsProduct(RestaurantEntity restaurantEntity) {
        return new Product(new ProductId(restaurantEntity.getProductId()),
                restaurantEntity.getProductName(),
                new Money(restaurantEntity.getProductPrice()));
    }
}
