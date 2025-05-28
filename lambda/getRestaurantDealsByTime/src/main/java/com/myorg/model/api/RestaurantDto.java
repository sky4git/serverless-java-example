import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RestaurantDto(
		@JsonProperty("objectId") int id,
		String name,
		String address1,
		String suburb,
		List<String> cuisines,
		String imageLink,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime open,
		@JsonFormat(pattern = "h:mma") @JsonDeserialize(using = LocalTimeDeserializer.class) @JsonSerialize(using = LocalTimeSerializer.class) LocalTime close,
		List<DealDto> deals) {
}
