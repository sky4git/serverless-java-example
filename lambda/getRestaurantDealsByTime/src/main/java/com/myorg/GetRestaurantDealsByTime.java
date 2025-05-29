package com.myorg;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.model.api.DealDto;
import com.myorg.model.api.RestaurantsApiDto;
import com.myorg.model.api.RestaurantDto;
import com.myorg.model.response.ResponseDealDto;
import com.myorg.model.response.ResponseDto;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class GetRestaurantDealsByTime
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		context.getLogger().log("Received event: " + event.toString());
		Map<String, String> queryParams = event.getQueryStringParameters();
		String time = queryParams != null ? queryParams.get("time") : null;
		if (time == null || time.isEmpty()) {
			return createResponse(400, "Missing required 'time' parameter");
		}
		LocalTime localTime;
		try {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder()
					.parseCaseInsensitive()
					.appendPattern("h:mma")
					.toFormatter(Locale.ENGLISH);

			localTime = LocalTime.parse(time, formatter);
		} catch (DateTimeParseException e) {
			return createResponse(400, "Invalid time format. Please use format like 10:20am or 10:20pm.");
		}

		List<ResponseDealDto> activeDeals = getActiveDeals(localTime);
		String responseBody = toJson(new ResponseDto(activeDeals));
		return createResponse(200, responseBody);
	}

	private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(statusCode);
		response.setBody(body);
		response.setHeaders(Map.of("Content-Type", "application/json"));
		return response;
	}

	private List<ResponseDealDto> getActiveDeals(LocalTime time) {
		RestaurantsApiDto restaurantsApiDto = fetchRestaurants();
		System.out.println("Fetched restaurants: " + toJson(restaurantsApiDto));
		// Filter restaurants that have active deals at the specified time
		if (restaurantsApiDto != null) {
			List<RestaurantDto> restaurantsWithActiveDeals = restaurantsApiDto.getRestaurantsByActiveDeals(time);
			return convertToResponseDealDtos(restaurantsWithActiveDeals);
		}
		return List.of();
	}

	private RestaurantsApiDto fetchRestaurants() {
		try {
			return mapper.readValue(
					new URL("https://eccdn.com.au/misc/challengedata.json"),
					RestaurantsApiDto.class);
		} catch (Exception e) {
			throw new RuntimeException("Error fetching restaurants", e);
		}
	}

	private List<ResponseDealDto> convertToResponseDealDtos(List<RestaurantDto> restaurants) {
		return restaurants.stream()
				.flatMap(restaurant -> restaurant.deals().stream().map(deal -> new ResponseDealDto(
						restaurant.id(),
						restaurant.name(),
						restaurant.address1(),
						restaurant.suburb(),
						restaurant.open() != null ? restaurant.open().toString() : null,
						restaurant.close() != null ? restaurant.close().toString() : null,
						deal.id(),
						deal.discount(),
						deal.dineIn(),
						deal.lightning(),
						deal.qtyLeft())))
				.collect(Collectors.toList());
	}

	private String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			return "{}";
		}
	}
}
