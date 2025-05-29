package com.myorg.model.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Locale;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalTimeSerializer extends StdSerializer<LocalTime> {
	private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
			.parseCaseInsensitive()
			.appendPattern("h:mma")
			.toFormatter(Locale.ENGLISH);

	public LocalTimeSerializer() {
		super(LocalTime.class);
	}

	@Override
	public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(value.format(formatter));
	}
}
