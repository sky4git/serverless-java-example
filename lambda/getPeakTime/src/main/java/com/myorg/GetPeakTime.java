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
			return Utils.createResponse(500, "Environment variable 'RESTAURANTS_API_URL' is not set.");
		}
		RestaurantsApiDto restaurantsApiDto;
		try {
			restaurantsApiDto = RestaurantsFetcher
					.fetchRestaurants(System.getenv("RESTAURANTS_API_URL"));
		} catch (Exception e) {
			return Utils.createResponse(500, "Failed to fetch restaurants.");
		}

		if (restaurantsApiDto == null) {
			return Utils.createResponse(500, "Failed to fetch restaurants.");
		}
		if (restaurantsApiDto.restaurants() == null || restaurantsApiDto.restaurants().isEmpty()) {
			return Utils.createResponse(500, "No restaurants found.");
		}

		try {
			PeakTimeWindowDto peakTimeWindow = findPeakTime(restaurantsApiDto.restaurants());
			return Utils.createResponse(200, Utils.toJson(peakTimeWindow));
		} catch (Exception e) {
			return Utils.createResponse(500, "Failed to determine peak time slot.");
		}
	}

	private PeakTimeWindowDto findPeakTime(List<RestaurantDto> restaurants) {
		// our calculatuion will be based on 15 minute time slots
		Duration peakDuration = Duration.ofMinutes(15);

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
		System.out.println("Deal counts: " + dealCounts);
		if (dealCounts.isEmpty()) {
			throw new IllegalStateException("No deals found to determine peak time slot.");
		}

		// find the maximum value of the deal numbers from the map
		int maxCount = dealCounts.values().stream().max(Integer::compareTo).orElse(0);
		if (maxCount == 0) {
			throw new IllegalStateException("No deals found to determine peak time slot.");
		}
		LocalTime longestStart = null;
		LocalTime longestEnd = null;
		LocalTime currentStart = null;
		LocalTime previousTime = null;

		for (Map.Entry<LocalTime, Integer> entry : dealCounts.entrySet()) {
			LocalTime time = entry.getKey();
			int count = entry.getValue();
			// check if the current count is equal to the maximum count
			if (count == maxCount) {
				// for first iteration, set the current start time
				if (currentStart == null) {
					currentStart = time;
				} else if (previousTime != null && !Duration.between(previousTime, time).equals(peakDuration)) {
					// reset the current start time if the gap is not equal to peakDuration (15 min
					// slot)
					currentStart = time;
				}
				longestEnd = time.plus(peakDuration);
				// for the first iteration, set the longest start time
				if (longestStart == null) {
					longestStart = currentStart;
				}
			} else {
				currentStart = null;
			}
			previousTime = time;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
		return new PeakTimeWindowDto(longestStart.format(formatter).toLowerCase(),
				longestEnd.format(formatter).toLowerCase());
	}
}
