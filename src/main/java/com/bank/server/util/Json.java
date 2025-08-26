
package com.bank.server.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.bank.server.config.HttpConfigurationException;

public class Json {
	private static final ObjectMapper objectMapper = defaultObjectMapper();

	public static ObjectMapper defaultObjectMapper() {
		ObjectMapper om = new ObjectMapper();

		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return om;
	}

	public static JsonNode parse(String jString) throws IOException {
		return objectMapper.readTree(jString);
	}

	public static String stringifyPretty(JsonNode jsonNode) throws JsonProcessingException {
		return generateJson(jsonNode, true);
	}

	public static String stringify(JsonNode jsonNode) throws JsonProcessingException {
		return generateJson(jsonNode, false);
	}

	private static String generateJson(JsonNode jsonNode, boolean pretty) throws JsonProcessingException {
		ObjectWriter ow = objectMapper.writer();
		if (pretty) {
			ow = ow.with(SerializationFeature.INDENT_OUTPUT);
		}

		return ow.writeValueAsString(jsonNode);
	}

	public static <A> A fromJson(JsonNode conf, Class<A> clazz) throws HttpConfigurationException {

		try {
			return objectMapper.treeToValue(conf, clazz);
		} catch (JsonProcessingException e) {
			throw new HttpConfigurationException("Processing Exception: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new HttpConfigurationException("IllegalArgumentException: " + e.getMessage());
		}
	}

	public static JsonNode toJson(Object object) {
		return objectMapper.valueToTree(object);
	}

}
