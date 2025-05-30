package com.myorg;

import com.amazonaws.services.lambda.runtime.Context;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.model.api.RestaurantsApiDto;
import com.myorg.model.api.RestaurantDto;
import com.myorg.model.PeakTimeWindowDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import java.util.List;
import static org.mockito.Mockito.mockStatic;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import com.myorg.RestaurantsFetcher;
import com.myorg.GetPeakTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.Map;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import java.time.LocalTime;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

@ExtendWith(SystemStubsExtension.class)
public class GetPeakTimeTest {

	@Test
	void testHandler_withNoEnvVariableSet() throws Exception {
		GetPeakTime handler = new GetPeakTime();
		new EnvironmentVariables("RESTAURANTS_API_URL", "")
				.execute(() -> {
					APIGatewayProxyResponseEvent response = handler.handleRequest(new APIGatewayProxyRequestEvent(),
							new DummyContext());
					assertEquals(500, response.getStatusCode());
					assertEquals("Environment variable 'RESTAURANTS_API_URL' is not set.", response.getBody());
				});
	}

	@Test
	public void testHandler_withMockedFetcher_failsToFetchData() throws Exception {
		// Create instance of handler
		GetPeakTime handler = new GetPeakTime();
		new EnvironmentVariables("RESTAURANTS_API_URL", "dummy")
				.execute(() -> {
					try (MockedStatic<RestaurantsFetcher> fetcherMock = mockStatic(RestaurantsFetcher.class)) {
						fetcherMock.when(() -> RestaurantsFetcher.fetchRestaurants("dummy"))
								.thenThrow(new RuntimeException("Failed to fetch data"));
						// Create a request event with a valid time query parameter
						APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
						requestEvent.setQueryStringParameters(Map.of("time", "3:20pm"));

						// Call handler and verify it returns 200 status code
						APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, new DummyContext());
						assertEquals(500, response.getStatusCode());
						assertEquals("Failed to fetch restaurants.", response.getBody());
					}
				});
	}

	@Test
	public void testHandler_withMockedFetcher_withEmptyRestaurantsList() throws Exception {
		// Create instance of handler
		GetPeakTime handler = new GetPeakTime();
		new EnvironmentVariables("RESTAURANTS_API_URL", "dummy")
				.execute(() -> {
					try (MockedStatic<RestaurantsFetcher> fetcherMock = mockStatic(RestaurantsFetcher.class)) {
						fetcherMock.when(() -> RestaurantsFetcher.fetchRestaurants("dummy"))
								.thenReturn(new RestaurantsApiDto(List.of()));
						// Create a request event with a valid time query parameter
						APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
						requestEvent.setQueryStringParameters(Map.of("time", "3:20pm"));

						// Call handler and verify it returns 200 status code
						APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, new DummyContext());
						assertEquals(500, response.getStatusCode());
						assertEquals("No restaurants found.", response.getBody());
					}
				});
	}

	@Test
	public void testHandler_withMockedFetcher_findsPeaktimeSlot() throws Exception {
		// Create instance of handler
		GetPeakTime handler = new GetPeakTime();
		String mockJson = readFile("src/test/resources/restaurants.json");
		ObjectMapper mapper = new ObjectMapper();
		RestaurantsApiDto dto = mapper.readValue(mockJson, RestaurantsApiDto.class);
		new EnvironmentVariables("RESTAURANTS_API_URL", "dummy")
				.execute(() -> {
					try (MockedStatic<RestaurantsFetcher> fetcherMock = mockStatic(RestaurantsFetcher.class)) {
						fetcherMock.when(() -> RestaurantsFetcher.fetchRestaurants("dummy")).thenReturn(dto);
						// Create a request event with a valid time query parameter
						APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
						requestEvent.setQueryStringParameters(Map.of("time", "3:20pm"));

						// Call handler and verify it returns 200 status code
						APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, new DummyContext());
						assertEquals(200, response.getStatusCode());
						assertEquals("application/json", response.getHeaders().get("Content-Type"));
						PeakTimeWindowDto peakTimeWindow = mapper.readValue(response.getBody(),
								PeakTimeWindowDto.class);
						assertNotNull(peakTimeWindow);
						assertEquals("5:00pm", peakTimeWindow.peakTimeStart());
						assertEquals("9:15pm", peakTimeWindow.peakTimeEnd());
					}
				});
	}

	@Test
	public void testHandler_withMockedFetcher_findsPeaktimeSlot_fails() throws Exception {
		// Create instance of handler
		GetPeakTime handler = new GetPeakTime();
		new EnvironmentVariables("RESTAURANTS_API_URL", "dummy")
				.execute(() -> {
					try (MockedStatic<RestaurantsFetcher> fetcherMock = mockStatic(RestaurantsFetcher.class)) {
						fetcherMock.when(() -> RestaurantsFetcher.fetchRestaurants("dummy"))
								.thenReturn(new RestaurantsApiDto(List.of(
										new RestaurantDto(
												"Restaurant id",
												"Test Restaurant",
												"Street",
												"Suburb",
												List.of(),
												"ImageURL",
												LocalTime.of(10, 0),
												LocalTime.of(23, 0),
												List.of()))));

						// Create a request event with a valid time query parameter
						APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
						requestEvent.setQueryStringParameters(Map.of("time", "3:20pm"));

						// Call handler and verify it returns 200 status code
						APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent,
								new DummyContext());
						assertEquals(500, response.getStatusCode());
						assertEquals("Failed to determine peak time slot.", response.getBody());
					}
				});
	}

	private String readFile(String path) {
		try {
			return new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	class DummyContext implements Context {
		@Override
		public String getAwsRequestId() {
			return "";
		}

		@Override
		public String getLogGroupName() {
			return "";
		}

		@Override
		public String getLogStreamName() {
			return "";
		}

		@Override
		public String getFunctionName() {
			return "";
		}

		@Override
		public String getFunctionVersion() {
			return "";
		}

		@Override
		public String getInvokedFunctionArn() {
			return "";
		}

		@Override
		public CognitoIdentity getIdentity() {
			return null;
		}

		@Override
		public ClientContext getClientContext() {
			return null;
		}

		@Override
		public int getRemainingTimeInMillis() {
			return 0;
		}

		@Override
		public int getMemoryLimitInMB() {
			return 0;
		}

		@Override
		public LambdaLogger getLogger() {
			return new LambdaLogger() {
				@Override
				public void log(String message) {
				}

				@Override
				public void log(byte[] message) {
				}
			};
		}
	}

}
