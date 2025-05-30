package com.myorg.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalTime;
import java.util.List;

public record RestaurantDto(
		@JsonProperty("objectId") String id,
		String name,
		String address1,
		String suburb,
		List<String> cuisines,
		String imageLink,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime open,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime close,
		List<DealDto> deals) {

	public List<DealDto> getActiveDeals(LocalTime time) {
		LocalTime restaurantOpenTime = (open != null) ? open : LocalTime.MIN;
		LocalTime restaurantCloseTime = (close != null) ? close : LocalTime.MAX;

		return deals.stream()
				.filter(deal -> deal.isAvailable(time, restaurantOpenTime, restaurantCloseTime))
				.toList();
	}
}
