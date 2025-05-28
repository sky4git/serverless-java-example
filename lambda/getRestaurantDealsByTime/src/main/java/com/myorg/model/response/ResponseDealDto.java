package com.myorg.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record ResponseDealDto(
		@JsonProperty("restaurantObjectId") String restaurantId,
		@JsonProperty("restaurantName") String name,
		@JsonProperty("restaurantAddress1") String address1,
		@JsonProperty("restaurantSuburb") String suburb,

		@JsonProperty("restaurantOpen") @JsonFormat(pattern = "h:mma", shape = JsonFormat.Shape.STRING) LocalTime open,
		@JsonProperty("restaurantClose") @JsonFormat(pattern = "h:mma", shape = JsonFormat.Shape.STRING) LocalTime close,

		@JsonProperty("dealObjectId") String id,
		Double discount,
		@JsonProperty("dineIn") Boolean isDineIn,
		@JsonProperty("lightning") Boolean isLightning,
		@JsonProperty("qtyLeft") String quantityLeft) {
}