package com.myorg;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.myorg.model.api.RestaurantDto;
import com.myorg.model.api.RestaurantsApiDto;
import com.myorg.model.response.ResponseDealDto;
import com.myorg.model.response.ResponseDto;
import com.myorg.Utils;
import com.myorg.RestaurantsFetcher;

public class GetRestaurantDealsByTime
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		context.getLogger().log("Received event: " + event.toString());
		// make sure we have the required environment variable set
		if (System.getenv("RESTAURANTS_API_URL") == null || System.getenv("RESTAURANTS_API_URL").isEmpty()) {
			// if not, throw an exception
			throw new IllegalStateException("Environment variable 'RESTAURANTS_API_URL' is not set.");
		}
		// validate that we have the time query parameter
		Map<String, String> queryParams = event.getQueryStringParameters();
		String time = queryParams != null ? queryParams.get("time") : null;
		if (time == null || time.isEmpty()) {
			return Utils.createResponse(400, "Missing required 'time' parameter");
		}

		// make sure the time is in the correct format
		LocalTime localTime;
		try {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder()
					.parseCaseInsensitive()
					.appendPattern("h:mma")
					.toFormatter(Locale.ENGLISH);

			localTime = LocalTime.parse(time, formatter);
		} catch (DateTimeParseException e) {
			return Utils.createResponse(400, "Invalid time format. Please use format like 10:20am or 10:20pm.");
		}

		try {
			// fetch the deals and provide the response
			List<ResponseDealDto> activeDeals = getActiveDeals(localTime);
			String responseBody = Utils.toJson(new ResponseDto(activeDeals));
			return Utils.createResponse(200, responseBody);
		} catch (Exception e) {
			return Utils.createResponse(500, "Failed to fetch restaurant deals.");
		}

	}

	private List<ResponseDealDto> getActiveDeals(LocalTime time) {
		RestaurantsApiDto restaurantsApiDto;
		try {
			restaurantsApiDto = RestaurantsFetcher
					.fetchRestaurants(System.getenv("RESTAURANTS_API_URL"));
			System.out.println("Fetched restaurants: " + Utils.toJson(restaurantsApiDto));
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch restaurants.", e);
		}
		// Filter restaurants that have active deals at the specified time
		if (restaurantsApiDto != null) {
			List<RestaurantDto> restaurantsWithActiveDeals = restaurantsApiDto.getRestaurantsByActiveDeals(time);
			return convertToResponseDealDtos(restaurantsWithActiveDeals);
		}
		return List.of();
	}

	private List<ResponseDealDto> convertToResponseDealDtos(List<RestaurantDto> restaurants) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
		return restaurants.stream()
				.flatMap(restaurant -> restaurant.deals().stream().map(deal -> new ResponseDealDto(
						restaurant.id(),
						restaurant.name(),
						restaurant.address1(),
						restaurant.suburb(),
						restaurant.open() != null ? restaurant.open().format(formatter).toLowerCase() : null,
						restaurant.close() != null ? restaurant.close().format(formatter).toLowerCase() : null,
						deal.id(),
						deal.discount(),
						deal.dineIn(),
						deal.lightning(),
						deal.qtyLeft())))
				.collect(Collectors.toList());
	}
}
