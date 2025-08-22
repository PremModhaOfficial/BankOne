package com.bank.clientInterface.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A utility class to map user input from the command line to Java objects.
 * It uses reflection to inspect the fields of a class and prompts the user
 * for each field's value, handling type validation and retries.
 * <p>
 * This is a simplified utility focusing on basic type safety and user
 * interaction.
 * It's not a full-fledged serialization framework like Jackson.
 */
public class CliObjectMapper {

    private final Scanner scanner;

    /**
     * Constructs a CliObjectMapper with a given Scanner.
     *
     * @param scanner The Scanner to use for reading user input.
     */
    public CliObjectMapper(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prompts the user to fill in the fields of an object of the specified class.
     *
     * @param clazz The Class of the object to create and populate.
     * @param <T>   The type of the object.
     * @return A new instance of T populated with user input.
     * @throws RuntimeException If object instantiation fails.
     */
    public <T> T readValue(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            populateObject(instance, clazz);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create or populate object of type: " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Prompts the user to fill in the fields of an existing object.
     *
     * @param instance The object instance to populate.
     * @param clazz    The Class of the object.
     * @param <T>      The type of the object.
     */
    private <T> void populateObject(T instance, Class<T> clazz) {

        System.out.println("Please provide values for the fields of " + clazz.getSimpleName() + ":");

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().toLowerCase().contains("id")) {
                continue;
            }

            field.setAccessible(true); // Allow access to private fields
            promptAndSetField(instance, field);
        }
    }

    /**
     * Prompts the user for a single field's value and sets it on the object.
     *
     * @param instance The object instance.
     * @param field    The field to set.
     * @param <T>      The type of the object.
     */
    private <T> void promptAndSetField(T instance, Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        String fullPrompt = "Enter value for '" + fieldName + "' (" + fieldType.getSimpleName() + "): ";

        Object value = null;
        boolean validInput = false;

        while (!validInput) {
            System.out.print(fullPrompt);
            String input = scanner.nextLine().trim();

            try {
                // Handle null/empty input for non-primitive types
                if (input.isEmpty() && !fieldType.isPrimitive()) {
                    value = null;
                    validInput = true;
                } else {
                    value = parseInput(input, fieldType, field);
                    validInput = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input type for '" + fieldName + "'. Expected: " + fieldType.getSimpleName()
                        + ". Please try again.");
            } catch (Exception e) {
                System.out.println("An error occurred while processing input for '" + fieldName + "': " + e.getMessage()
                        + ". Please try again.");
            }
        }

        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            // This should not happen due to setAccessible(true)
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    /**
     * Parses the string input into the appropriate object type based on the field
     * type.
     *
     * @param input     The user input string.
     * @param fieldType The expected type of the field.
     * @param field     The Field object (used for generic type information).
     * @return The parsed object.
     * @throws NumberFormatException    If number parsing fails.
     * @throws IllegalArgumentException If input is invalid for the type.
     */
    private Object parseInput(String input, Class<?> fieldType, Field field) throws Exception {
        if (fieldType == String.class) {
            return input; // Strings are always valid
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(input);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(input);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(input);
        } else if (fieldType == float.class || fieldType == Float.class) {
            return Float.parseFloat(input);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            // Accept "true", "false", "1", "0" (case-insensitive)
            if ("true".equalsIgnoreCase(input) || "1".equals(input)) {
                return true;
            } else if ("false".equalsIgnoreCase(input) || "0".equals(input)) {
                return false;
            } else {
                throw new IllegalArgumentException("Invalid boolean value");
            }
        } else if (fieldType == byte.class || fieldType == Byte.class) {
            return Byte.parseByte(input);
        } else if (fieldType == short.class || fieldType == Short.class) {
            return Short.parseShort(input);
        } else if (fieldType == char.class || fieldType == Character.class) {
            if (input.length() == 1) {
                return input.charAt(0);
            } else {
                throw new IllegalArgumentException("Input must be a single character");
            }
        } else if (fieldType == BigDecimal.class) {
            return new BigDecimal(input);
        } else if (fieldType.isEnum()) {
            // This is a bit more complex for enums
            return parseEnum(input, (Class<? extends Enum>) fieldType);
        } else if (List.class.isAssignableFrom(fieldType)) {
            // Handle simple list of strings for now
            // For more complex types, we'd need generic type information
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                if (typeArguments.length > 0) {
                    Class<?> listElementType = (Class<?>) typeArguments[0];
                    return parseList(input, listElementType, field);
                }
            }
            // Default to List<String>
            return parseList(input, String.class, field);
        } else {
            // For complex objects, recursively call readValue
            // This requires the class to have a no-arg constructor
            System.out.println("Entering nested object '" + fieldType.getSimpleName() + "':");
            Object nestedInstance = fieldType.getDeclaredConstructor().newInstance();
            populateObject(nestedInstance, (Class<? super Object>) fieldType);
            return nestedInstance;
        }
    }

    /**
     * Parses an enum value.
     *
     * @param input     The user input string.
     * @param enumClass The enum class.
     * @return The corresponding enum constant.
     * @throws IllegalArgumentException If the input does not match any enum
     *                                  constant.
     */
    private <E extends Enum<E>> E parseEnum(String input, Class<E> enumClass) {
        try {
            return Enum.valueOf(enumClass, input);
        } catch (IllegalArgumentException e) {
            // Provide a helpful error message listing valid options
            StringBuilder validOptions = new StringBuilder();
            for (E enumConstant : enumClass.getEnumConstants()) {
                if (validOptions.length() > 0) {
                    validOptions.append(", ");
                }
                validOptions.append(enumConstant.name());
            }
            throw new IllegalArgumentException("Invalid value for enum " + enumClass.getSimpleName()
                    + ". Valid options are: " + validOptions.toString());
        }
    }

    /**
     * Parses a list from a comma-separated string input.
     * This is a simple implementation. For more complex element types,
     * a more sophisticated parsing mechanism would be needed.
     *
     * @param input       The user input string (comma-separated).
     * @param elementType The type of elements in the list.
     * @param field       The Field object (for context if needed).
     * @return A List of the specified element type.
     * @throws Exception If parsing fails.
     */
    private List<?> parseList(String input, Class<?> elementType, Field field) throws Exception {
        List<Object> list = new ArrayList<>();
        if (input.isEmpty()) {
            return list; // Return empty list
        }

        String[] items = input.split(",");
        for (String item : items) {
            item = item.trim();
            if (!item.isEmpty()) {
                // Recursively parse each item based on its type
                // This is a simplified approach for basic types
                Object parsedItem = parseInput(item, elementType, field);
                list.add(parsedItem);
            }
        }
        return list;
    }
}
