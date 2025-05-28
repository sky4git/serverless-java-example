import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record DealDto(
		@JsonProperty("objectId") String id,
		String discount,
		String dineIn,
		String lightning,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime open,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime close,
		String qtyLeft) {

	// get discount percentage to check discount validity
	public Double discountPercentage() {
		try {
			return Double.parseDouble(discountStr);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}

	// We need to check if the quantity is valid and greater than 0 for deal to be
	// available
	public Boolean isValidQuantity() {
		try {
			return Integer.parseInt(quantityLeft) > 0;
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}

}