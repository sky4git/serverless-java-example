package com.myorg.model.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Locale;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalTimeDeserializer extends StdDeserializer<LocalTime> {
	private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
			.parseCaseInsensitive()
			.appendPattern("h:mma")
			.toFormatter(Locale.ENGLISH);

	public LocalTimeDeserializer() {
		super(LocalTime.class);
	}

	@Override
	public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		return LocalTime.parse(p.getText(), formatter);
	}
}
