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

	public boolean isValidDiscountPercentage() {
		try {
			return validDiscountPercentage ? validDiscountPercentage : Double.parseDouble(discount) > 0;
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

	// We need to check if the quantity is valid and greater than 0 for deal to be
	// available
	public boolean isValidQuantity() {
		return validQuantity != null ? validQuantity : quantityLeft() > 0;
	}

	// Is the deal available by time
	// considers the edge cases of the exact open & close time being valid
	public boolean isAvailableByTime(LocalTime time) {
		LocalTime startTime = (start != null) ? start : open;
		LocalTime endTime = (end != null) ? end : close;
		if (startTime == null || endTime == null) {
			// If neither start/end nor open/close are set, treat as always available
			return true;
		}
		// Inclusive at both ends
		return (!time.isBefore(startTime)) && (!time.isAfter(endTime));
	}

	public boolean isAvailable(LocalTime time, LocalTime restaurantOpen, LocalTime restaurantClose) {
		LocalTime startTime = (start != null) ? start : open;
		LocalTime endTime = (end != null) ? end : close;

		if (startTime == null && endTime == null) {
			// No deal-specific time, fall back to restaurant hours
			if (restaurantOpen != null && restaurantClose != null) {
				return !time.isBefore(restaurantOpen) && !time.isAfter(restaurantClose);
			}
			// If no restaurant hours, treat as always available
			return true;
		}
		if (startTime != null && endTime != null) {
			return !time.isBefore(startTime) && !time.isAfter(endTime);
		}
		if (startTime != null) {
			return !time.isBefore(startTime);
		}
		if (endTime != null) {
			return !time.isAfter(endTime);
		}
		return true;
	}

}