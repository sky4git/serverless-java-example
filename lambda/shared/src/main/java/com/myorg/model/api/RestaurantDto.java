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
		String open,
		String close,
		List<DealDto> deals) {

	public List<DealDto> getActiveDeals(LocalTime time) {
		return deals.stream().filter(deal -> deal.isAvailable(time)).toList();
	}
}
