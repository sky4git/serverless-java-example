package com.myorg;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Locale;
import java.time.Duration;
import com.myorg.RestaurantsFetcher;
import com.myorg.model.api.RestaurantDto;
import com.myorg.model.api.RestaurantsApiDto;
import com.myorg.model.api.DealDto;
import com.myorg.model.PeakTimeWindowDto;

public class GetPeakTime implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		context.getLogger().log("Received event: " + event.toString());

		if (System.getenv("RESTAURANTS_API_URL") == null || System.getenv("RESTAURANTS_API_URL").isEmpty()) {
			throw new IllegalStateException("Environment variable 'RESTAURANTS_API_URL' is not set.");
		}

		RestaurantsApiDto restaurantsApiDto = RestaurantsFetcher.fetchRestaurants(System.getenv("RESTAURANTS_API_URL"));
		if (restaurantsApiDto == null) {
			throw new IllegalStateException("Failed to fetch restaurants.");
		}
		if (restaurantsApiDto.restaurants() == null || restaurantsApiDto.restaurants().isEmpty()) {
			throw new IllegalStateException("No restaurants found.");
		}
		PeakTimeWindowDto peakTimeWindow = findPeakTime(restaurantsApiDto.restaurants());
		return Utils.createResponse(200, Utils.toJson(peakTimeWindow));
	}

	private PeakTimeWindowDto findPeakTime(List<RestaurantDto> restaurants) {
		Duration peakDuration = Duration.ofHours(1);
		Map<LocalTime, Integer> dealCounts = new TreeMap<>();
		LocalTime peakStart = LocalTime.MIN;
		LocalTime peakEnd = LocalTime.MAX.minus(peakDuration);
		while (peakStart.isBefore(peakEnd)) {
			final LocalTime slotTime = peakStart;
			int count = (int) restaurants.stream()
					.flatMap(restaurant -> restaurant.deals().stream())
					.filter(deal -> deal.isAvailableByTime(slotTime))
					.count();
			dealCounts.put(slotTime, count);
			peakStart = peakStart.plus(peakDuration);
		}
		if (dealCounts.isEmpty()) {
			throw new IllegalStateException("No deals found.");
		}
		Map.Entry<LocalTime, Integer> maxEntry = dealCounts.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.orElseThrow(() -> new IllegalStateException("No peak time found."));
		LocalTime peakStartTime = maxEntry.getKey();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
		return new PeakTimeWindowDto(peakStartTime.format(formatter),
				peakStartTime.plus(peakDuration).format(formatter));
	}
}
