package com.bank.clientInterface.util;

import com.bank.server.util.Json;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;

/**
 * Utility class for formatting HTTP responses for better readability
 */
public class ResponseFormatter {
    /**
     * Formats JSON response for better readability
     * 
     * @param jsonString The JSON string to format
     * @return Formatted JSON string or original string if formatting fails
     */
    public static String formatJsonResponse(String jsonString) {
        try {
            if (jsonString == null || jsonString.isEmpty()) {
                return "No response body";
            }

            // Try to parse and format as JSON
            JsonNode jsonNode = Json.parse(jsonString);
            return Json.stringifyPretty(jsonNode);
        } catch (Exception e) {
            // If parsing fails, return the original string
            return jsonString;
        }
    }

    /**
     * Logs and displays a formatted response
     * 
     * @param logger        The logger to use for logging
     * @param operationName The name of the operation being performed
     * @param statusCode    The HTTP status code
     * @param responseBody  The response body
     */
    public static void logAndDisplayResponse(Logger logger, String operationName, int statusCode, String responseBody) {
        logger.debug("{} Response Status: {}", operationName, statusCode);
        logger.debug("{} Response Body:\n{}", operationName, formatJsonResponse(responseBody));
    }

    public static void logResponse(Logger logger, String operationName, int statusCode, String responseBody) {
        logger.debug("{} Response Status: {}", operationName, statusCode);
    }

    /**
     * Displays a formatted response to the console
     * 
     * @param operationName The name of the operation being performed
     * @param statusCode    The HTTP status code
     * @param responseBody  The response body
     */
    public static void displayResponse(String operationName, int statusCode, String responseBody) {
        System.out.println(operationName + " Response Status: " + statusCode);
        if (statusCode >= 200 && statusCode < 300) {
            System.out.println("Response:");
            System.out.println(formatJsonResponse(responseBody));
        } else {
            System.out.println("Failed. Server responded with status: " + statusCode);
            System.out.println("Response Body: " + responseBody);
        }
    }
}
