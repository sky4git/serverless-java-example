package com.myorg.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalTime;

public record DealDto(
		@JsonProperty("objectId") String id,
		String discount,
		Boolean dineIn,
		Boolean lightning,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime open,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime close,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime start,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime end,
		String qtyLeft,
		Boolean validQuantity,
		Boolean validDiscountPercentage) {

	public Boolean isValidDiscountPercentage() {
		try {
			return validDiscountPercentage != null ? validDiscountPercentage : Double.parseDouble(discount) > 0;
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}

	public Integer quantityLeft() {
		try {
			return Integer.parseInt(qtyLeft);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}

	// We need to check if the quantity is valid and greater than 0 for deal to be available
	public Boolean isValidQuantity() {
		return validQuantity != null ? validQuantity : quantityLeft() > 0;
	}

	// Is the deal available by time
	// considers the edge cases of the exact open & close time being valid
	public Boolean isAvailableByTime(LocalTime time) {
		LocalTime startTime = start != null ? start : open;
		LocalTime endTime = end != null ? end : close;
		if (startTime == null || endTime == null) {
			return false; // If either start or end time is null, the deal is not available
		}
		return (time.equals(startTime) || (time.isAfter(startTime) && time.isBefore(endTime)) || time.equals(endTime));
	}

	public Boolean isAvailable(LocalTime time) {
		// Check if the deal is valid based on quantity, discount percentage, and time
		return isValidQuantity() && isValidDiscountPercentage() && isAvailableByTime(time);
	}

}