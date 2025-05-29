package com.myorg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {
	record Dummy(String name, int value) {
	}

	@Test
	public void testToJson_withValidObject_returnsJsonString() {
		Dummy dummy = new Dummy("foo", 42);
		String json = Utils.toJson(dummy);
		assertTrue(json.contains("foo"));
		assertTrue(json.contains("42"));
	}

	@Test
	public void testToJson_withNull_returnsEmptyJson() {
		String json = Utils.toJson(null);
		assertEquals("{}", json);
	}

}
