package com.myorg;

import java.net.URL;
import com.myorg.model.api.RestaurantsApiDto;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestaurantsFetcher {
	private static final ObjectMapper mapper = new ObjectMapper();

	public static RestaurantsApiDto fetchRestaurants(String baseUrl) {
		try {
			return mapper.readValue(new URL(baseUrl), RestaurantsApiDto.class);
		} catch (Exception e) {
			throw new RuntimeException("Error fetching restaurants", e);
		}
	}

}
