import com.amazonaws.services.lambda.runtime.Context;
import com.myorg.GetRestaurantDealsByTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import java.util.Map;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.myorg.model.response.ResponseDealDto;
import com.myorg.model.response.ResponseDto;
import com.myorg.model.api.RestaurantsApiDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.RestaurantsFetcher;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SystemStubsExtension.class)
public class GetRestaurantDealsByTimeTest {

	@Test
	void testHandler_withNoEnvVariableSet() throws Exception {
		// Create instance of handler
		GetRestaurantDealsByTime handler = new GetRestaurantDealsByTime();

		new EnvironmentVariables("RESTAURANTS_API_URL", "")
				.execute(() -> {
					// Call handler and verify it throws IllegalStateException
					assertThrows(IllegalStateException.class, () -> {
						handler.handleRequest(new APIGatewayProxyRequestEvent(), new DummyContext());
					}, "Handler should throw IllegalStateException when environment variable is not set");

					// Verify error message
					try {
						handler.handleRequest(new APIGatewayProxyRequestEvent(), new DummyContext());
					} catch (IllegalStateException e) {
						assertEquals("Environment variable 'RESTAURANTS_API_URL' is not set.", e.getMessage());
					}
				});

	}

	@Test
	public void testHandler_withNoTimeQueryParameter() throws Exception {
		// Create instance of handler
		GetRestaurantDealsByTime handler = new GetRestaurantDealsByTime();
		new EnvironmentVariables("RESTAURANTS_API_URL", "dummy")
				.execute(() -> {
					// Create a request event without the 'time' query parameter
					APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
					requestEvent.setQueryStringParameters(null);

					// Call handler and verify it returns 400 status code
					APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, new DummyContext());
					assertEquals(400, response.getStatusCode());
					assertTrue(response.getBody().contains("Missing required 'time' parameter"));
				});

	}

	@Test
	public void testHandler_withMalformedTimeQueryParameter() throws Exception {
		// Create instance of handler
		GetRestaurantDealsByTime handler = new GetRestaurantDealsByTime();
		new EnvironmentVariables("RESTAURANTS_API_URL", "dummy")
				.execute(() -> {
					// Create a request event without the 'time' query parameter
					APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
					requestEvent.setQueryStringParameters(Map.of("time", "invalid-time"));

					// Call handler and verify it returns 400 status code
					APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, new DummyContext());
					assertEquals(400, response.getStatusCode());
					assertTrue(response.getBody()
							.contains("Invalid time format. Please use format like 10:20am or 10:20pm."));
				});

	}

	@Test
	public void testHandler_withMockedFetcher() throws Exception {
		// Create instance of handler
		GetRestaurantDealsByTime handler = new GetRestaurantDealsByTime();
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

						// Mock the fetchRestaurants method to return the DTO
						when(RestaurantsFetcher.fetchRestaurants("dummy")).thenReturn(dto);

						// Call handler and verify it returns 200 status code
						APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, new DummyContext());
						assertEquals(200, response.getStatusCode());
						assertEquals("application/json", response.getHeaders().get("Content-Type"));
						System.out.println("Response body: " + response.getBody());
						// Verify the response body contains the expected deals
						ResponseDto responseDto = mapper.readValue(response.getBody(), ResponseDto.class);
						List<ResponseDealDto> deals = responseDto.deals();
						assertFalse(deals.isEmpty());
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
