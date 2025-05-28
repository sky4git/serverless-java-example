public class GetRestaurantDealsByTime {
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		// Log event for debugging
		context.getLogger().log("Received event: " + event.toString());

		// Extract the 'time' parameter from query parameters
		Map<String, String> queryParams = event.getQueryStringParameters();
		String time = queryParams != null ? queryParams.get("time") : null;

		// If time is null or empty, return an error response
		if (time == null || time.isEmpty()) {
			return createResponse(400, "Missing required 'time' parameter");
		}

		// Get the relevant deals
		List<Deal> activeDeals = getActiveDeals(time);

		// Convert to JSON and return
		String responseBody = new Gson().toJson(activeDeals);
		return createResponse(200, responseBody);
	}

	private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(statusCode);
		response.setBody(body);
		response.setHeaders(Map.of("Content-Type", "application/json"));
		return response;
	}

	private List<Deal> getActiveDeals(String time) {
		// logic to find deals active at the specified time
		// TODO
		return new ArrayList<>();
	}
}
