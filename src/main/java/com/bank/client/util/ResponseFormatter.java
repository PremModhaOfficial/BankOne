package com.bank.client.util;

import com.bank.server.util.Json;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for formatting HTTP responses for better readability
 */
public class ResponseFormatter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFormatter.class);

    /**
     * Formats JSON response for better readability
     * 
     * @param jsonString The JSON string to format
     * @return Formatted JSON string or original string if formatting fails
     */
    public static String formatJsonResponse(String jsonString)
    {
        try
        {
            if (jsonString == null || jsonString.isEmpty())
            {
                return "No response body";
            }

            // Try to parse and format as JSON
            var jsonNode = Json.parse(jsonString);
            return Json.stringifyPretty(jsonNode);
        } catch (Exception e)
        {
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
    public static void logAndDisplayResponse(Logger logger, String operationName, int statusCode, String responseBody)
    {
        logger.debug("{} Response Status: {}", operationName, statusCode);
        logger.debug("{} Response Body:\n{}", operationName, formatJsonResponse(responseBody));
    }

    public static void logResponse(Logger logger, String operationName, int statusCode, String responseBody)
    {
        logger.debug("{} Response Status: {}", operationName, statusCode);
    }

    /**
     * Displays a formatted response to the console
     *
     * @param operationName The name of the operation being performed
     * @param statusCode    The HTTP status code
     * @param responseBody  The response body
     */
    public static void displayResponse(String operationName, int statusCode, String responseBody)
    {
        LOGGER.debug("{} Response Status: {}", operationName, statusCode);
        LOGGER.debug("{} Response Body:\n{}", operationName, formatJsonResponse(responseBody));

        if (statusCode >= 200 && statusCode < 300)
        {
            System.out.println(operationName + " completed successfully.");
        } else
        {
            System.out.println(operationName + " failed. Status: " + statusCode);
        }
    }

    /**
     * Formats JSON array response as a clean table, filtering out unwanted fields
     *
     * @param jsonString    The JSON string containing array data
     * @param excludeFields Fields to exclude from display (e.g., "readWriteLock")
     * @return Formatted table string or original string if formatting fails
     */
    public static String formatJsonArrayTable(String jsonString, String... excludeFields)
    {
        try
        {
            if (jsonString == null || jsonString.isEmpty())
            {
                return "No data found";
            }

            // Parse JSON array
            var jsonNode = Json.parse(jsonString);
            if (!jsonNode.isArray() || jsonNode.size() == 0)
            {
                return formatJsonResponse(jsonString);
            }

            // Get the first object to determine field structure
            var firstObject = jsonNode.get(0);
            var fieldNames = new ArrayList<String>();

            // Collect all field names except excluded ones
            firstObject.fieldNames().forEachRemaining(fieldName -> {
                var exclude = false;
                for (String excludeField : excludeFields)
                {
                    if (fieldName.equals(excludeField))
                    {
                        exclude = true;
                        break;
                    }
                }
                if (!exclude)
                {
                    fieldNames.add(fieldName);
                }
            });

            if (fieldNames.isEmpty())
            {
                return "No displayable fields found";
            }

            return formatGenericTable(jsonNode, fieldNames);
        } catch (Exception e)
        {
            LOGGER.warn("Failed to format JSON array table: {}", e.getMessage());
            return formatJsonResponse(jsonString);
        }
    }

    /**
     * Formats account list response as a clean table without lock information
     *
     * @param jsonString The JSON string containing account list
     * @return Formatted table string or original string if formatting fails
     */
    public static String formatAccountList(String jsonString)
    {
        return formatJsonArrayTable(jsonString, "readWriteLock");
    }

    /**
     * Formats user list response as a clean table
     *
     * @param jsonString The JSON string containing user list
     * @return Formatted table string or original string if formatting fails
     */
    public static String formatUserList(String jsonString)
    {
        return formatJsonArrayTable(jsonString);
    }

    /**
     * Creates a formatted table from JSON array data
     *
     * @param jsonArray  The JSON array to format
     * @param fieldNames List of field names to display
     * @return Formatted table string
     */
    private static String formatGenericTable(JsonNode jsonArray, List<String> fieldNames)
    {
        var sb = new StringBuilder();

        // Calculate column widths
        var colWidths = new int[fieldNames.size()];
        for (var i = 0; i < fieldNames.size(); i++)
        {
            var fieldName = fieldNames.get(i);
            colWidths[i] = Math.max(fieldName.length(), 8); // Minimum width of 8

            // Check all rows for max content width
            for (JsonNode item : jsonArray)
            {
                if (item.has(fieldName))
                {
                    var value = item.get(fieldName);
                    var valueStr = formatJsonValue(value);
                    colWidths[i] = Math.max(colWidths[i], valueStr.length());
                }
            }
        }

        // Create table header
        sb.append(createTableBorder(colWidths, "┌", "┬", "┐")).append("\n");

        // Header row
        sb.append("│");
        for (var i = 0; i < fieldNames.size(); i++)
        {
            var fieldName = capitalizeFirst(fieldNames.get(i));
            sb.append(String.format(" %-" + colWidths[i] + "s │", fieldName));
        }
        sb.append("\n");

        // Separator row
        sb.append(createTableBorder(colWidths, "├", "┼", "┤")).append("\n");

        // Data rows
        for (var item : jsonArray)
        {
            sb.append("│");
            for (var i = 0; i < fieldNames.size(); i++)
            {
                var fieldName = fieldNames.get(i);
                var value = "";
                if (item.has(fieldName))
                {
                    value = formatJsonValue(item.get(fieldName));
                }
                sb.append(String.format(" %-" + colWidths[i] + "s │", value));
            }
            sb.append("\n");
        }

        // Table footer
        sb.append(createTableBorder(colWidths, "└", "┴", "┘")).append("\n");

        return sb.toString();
    }

    /**
     * Creates table border with specified characters
     */
    private static String createTableBorder(int[] colWidths, String left, String middle, String right)
    {
        var sb = new StringBuilder();
        sb.append(left);
        for (var i = 0; i < colWidths.length; i++)
        {
            for (var j = 0; j < colWidths[i] + 2; j++)
            {
                sb.append("─");
            }
            if (i < colWidths.length - 1)
            {
                sb.append(middle);
            }
        }
        sb.append(right);
        return sb.toString();
    }

    /**
     * Formats JSON value to string
     */
    private static String formatJsonValue(JsonNode value)
    {
        if (value.isTextual())
        {
            return value.asText();
        } else if (value.isBoolean())
        {
            return String.valueOf(value.asBoolean());
        } else if (value.isNumber())
        {
            // Format numbers nicely
            if (value.isDouble() || value.isFloat())
            {
                var doubleValue = value.asDouble();
                if (doubleValue == (long) doubleValue)
                {
                    return String.valueOf((long) doubleValue);
                } else
                {
                    return String.format("%.2f", doubleValue);
                }
            } else
            {
                return value.asText();
            }
        } else if (value.isNull())
        {
            return "";
        } else
        {
            return value.toString();
        }
    }

    /**
     * Capitalizes first letter of a string
     */
    private static String capitalizeFirst(String str)
    {
        if (str == null || str.isEmpty())
        {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
