package com.myorg;

import com.fasterxml.jackson.databind.ObjectMapper;

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
}
