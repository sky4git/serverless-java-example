package com.myorg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Map;

public class Utils {
	private static final ObjectMapper mapper = new ObjectMapper();

	public static String toJson(Object obj) {
		if (obj == null) {
			return "{}";
		}
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			return "{}";
		}
	}

	public static APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(statusCode);
		response.setBody(body);
		response.setHeaders(Map.of("Content-Type", "application/json"));
		return response;
	}
}
