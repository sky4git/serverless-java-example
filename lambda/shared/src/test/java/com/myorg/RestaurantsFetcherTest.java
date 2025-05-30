package com.myorg;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.myorg.model.api.RestaurantsApiDto;
import com.myorg.model.api.RestaurantDto;
import java.time.LocalTime;
import java.util.List;

public class RestaurantsFetcherTest {
	private static MockWebServer server;

	@BeforeAll
	public static void setup() throws IOException {
		server = new MockWebServer();
		server.start();
	}

	@AfterAll
	static void shutdownServer() throws IOException {
		server.shutdown();
	}

	@Test
	public void testFetchRestaurants_withValidUrl_returnsRestaurantsApiDto() {
		String mockJson = readFile("src/test/resources/restaurants.json");
		server.enqueue(new MockResponse()
				.setBody(mockJson)
				.addHeader("Content-Type", "application/json"));
		String baseUrl = server.url("/api/restaurants").toString();

		RestaurantsApiDto result = RestaurantsFetcher.fetchRestaurants(baseUrl);
		assertNotNull(result);
		assertFalse(result.restaurants().isEmpty());
		assertEquals(6, result.restaurants().size());
	}

	@Test
	public void testFetchRestaurants_throwsRuntimeException() {
		server.enqueue(new MockResponse()
				.setBody("not a json")
				.setResponseCode(200)
				.addHeader("Content-Type", "application/json"));

		String baseUrl = server.url("/bad").toString();

		RuntimeException ex = assertThrows(RuntimeException.class, () -> RestaurantsFetcher.fetchRestaurants(baseUrl));
		assertTrue(ex.getMessage().contains("Error fetching"));
	}

	@Test
	public void testFetchRestaurants_getRestaurantsWithActiveDeals() {
		String mockJson = readFile("src/test/resources/restaurants.json");
		server.enqueue(new MockResponse()
				.setBody(mockJson)
				.addHeader("Content-Type", "application/json"));
		String baseUrl = server.url("/api/restaurants").toString();

		RestaurantsApiDto result = RestaurantsFetcher.fetchRestaurants(baseUrl);
		assertNotNull(result);
		List<RestaurantDto> restaurantsWithActiveDeals = result.getRestaurantsByActiveDeals(LocalTime.of(15, 20));
		assertEquals(4, restaurantsWithActiveDeals.size());
	}

	private String readFile(String path) {
		try {
			return new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
