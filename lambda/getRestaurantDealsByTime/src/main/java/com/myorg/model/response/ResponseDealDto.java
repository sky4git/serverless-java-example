package com.myorg.model.response;

public record ResponseDealDto(
		String restaurantObjectId,
		String restaurantName,
		String restaurantAddress1,
		String restaurantSuburb,
		String restaurantOpen,
		String restaurantClose,
		String dealObjectId,
		String discount,
		Boolean dineIn,
		Boolean lightning,
		String qtyLeft) {
}