package com.myorg.model.api;

import java.util.List;
import java.time.LocalTime;

public record RestaurantsApiDto(
		List<RestaurantDto> restaurants) {

	// Get the list of restaurants that have active deals at the given time
	public List<RestaurantDto> getRestaurantsByActiveDeals(LocalTime time) {
		return restaurants.stream()
				.filter(restaurant -> restaurant.getActiveDeals(time).stream().findAny().isPresent())
				.map(restaurant -> new RestaurantDto(
						restaurant.id(),
						restaurant.name(),
						restaurant.address1(),
						restaurant.suburb(),
						restaurant.cuisines(),
						restaurant.imageLink(),
						restaurant.open(),
						restaurant.close(),
						restaurant.getActiveDeals(time)))
				.toList();
	}
}
