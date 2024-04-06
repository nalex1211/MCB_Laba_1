package org.example;

import org.example.JsonParser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class JsonParserTest {

    private JsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new JsonParser();
    }

    @Test
    void parseValidJsonObject() throws ParseException {
        String json = "{\"name\":\"John\",\"age\":30, \"isMarried\":false, \"children\":[\"Anna\", \"Mike\"]}";
        Object result = parser.parse(json);
        assertTrue(result instanceof Map);

        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("John", resultMap.get("name"));
        assertThat(resultMap.get("age"), is(equalTo(new BigDecimal("30"))));
        assertThat(resultMap, hasEntry(equalTo("isMarried"), equalTo(false)));
        assertThat((List<String>) resultMap.get("children"), containsInAnyOrder("Anna", "Mike"));
    }

    @Test
    void parseValidJsonArray() throws ParseException {
        String json = "[{\"fruit\":\"apple\"}, {\"fruit\":\"banana\"}]";
        Object result = parser.parse(json);
        assertTrue(result instanceof List);

        List<Map<String, String>> resultList = (List<Map<String, String>>) result;
        assertThat(resultList, hasSize(2));
        assertThat(resultList, contains(
                hasEntry("fruit", "apple"),
                hasEntry("fruit", "banana")
        ));
    }

    @Test
    void parseThrowsParseException() {
        String json = "{\"name\":\"John\"";
        assertThrows(ParseException.class, () -> parser.parse(json));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"string with \\\"quotes\\\"\"", "\"string with \\\\ backslashes\""})
    void parseStringsWithEscapedCharacters(String json) throws ParseException {
        Object result = parser.parse(json);
        assertTrue(result instanceof String);

        String expected = json.substring(1, json.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        assertEquals(expected, result);
    }

    @Test
    void prettyPrint() throws ParseException {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("name", "John");
        jsonMap.put("age", 30);
        jsonMap.put("phoneNumbers", Arrays.asList("123-456-7890", "456-789-0123"));

        String pretty = parser.prettyPrint(jsonMap);
        assertThat(pretty, allOf(containsString("name=John"), containsString("age=30"), containsString("phoneNumbers=[123-456-7890, 456-789-0123]")));
    }

    @Test
    void parseInvalidJsonThrowsParseException() {
        String json = "{ name: \"John\", age: 30 }";
        assertThrows(ParseException.class, () -> parser.parse(json));
    }

    @Test
    void parseComplexJson() throws ParseException {
        String json = "{\"person\":{\"name\":\"John\",\"age\":30,\"address\":{\"street\":\"123 Main St\",\"city\":\"Anytown\"}}}";
        Object result = parser.parse(json);
        assertThat(result, isA(Map.class));

        Map<?, ?> personMap = (Map<?, ?>) ((Map<?, ?>) result).get("person");
        assertThat(personMap, allOf(
                hasKey("name"),
                hasKey("age"),
                hasKey("address")
        ));

        Map<?, ?> addressMap = (Map<?, ?>) personMap.get("address");
        assertThat(addressMap, allOf(
                hasEntry(equalTo("street"), equalTo("123 Main St")),
                hasEntry(equalTo("city"), equalTo("Anytown"))
        ));
    }

    @Test
    void parseNullValue() throws ParseException {
        String json = "{\"key\":null}";
        Object result = parser.parse(json);
        assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertNull(resultMap.get("key"));
    }

    @Test
    void parseBooleanValues() throws ParseException {
        String json = "{\"trueValue\":true, \"falseValue\":false}";
        Object result = parser.parse(json);
        assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertTrue((Boolean) resultMap.get("trueValue"));
        assertFalse((Boolean) resultMap.get("falseValue"));
    }

    @Test
    void parseNumbers() throws ParseException {
        String json = "{\"integer\":42, \"floatingPoint\":3.14}";
        Object result = parser.parse(json);
        assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals(new BigDecimal("42"), resultMap.get("integer"));
        assertEquals(new BigDecimal("3.14"), resultMap.get("floatingPoint"));
    }

    @Test
    void parseMalformedJsonThrowsParseException() {
        String json = "{\"key\":42,";
        assertThrows(ParseException.class, () -> parser.parse(json));
    }

    @Test
    void parseEmptyJsonObject() throws ParseException {
        String json = "{}";
        Object result = parser.parse(json);
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    void parseEmptyJsonArray() throws ParseException {
        String json = "[]";
        Object result = parser.parse(json);
        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void parseNestedArrays() throws ParseException {
        String json = "[[1, 2], [3, 4]]";
        Object result = parser.parse(json);
        assertTrue(result instanceof List);

        List<?> resultList = (List<?>) result;
        assertThat(resultList, hasSize(2));
        assertThat((List<?>) resultList.get(0), contains(new BigDecimal("1"), new BigDecimal("2")));
        assertThat((List<?>) resultList.get(1), contains(new BigDecimal("3"), new BigDecimal("4")));
    }


    @Test
    void parseDifferentNumberTypes() throws ParseException {
        String json = "{\"longNumber\": 9223372036854775807, \"integerNumber\": 2147483647}";
        Object result = parser.parse(json);
        assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertThat(resultMap.get("longNumber"), is(equalTo(new BigDecimal("9223372036854775807"))));
        assertThat(resultMap.get("integerNumber"), is(equalTo(new BigDecimal("2147483647"))));
    }


    @Test
    void parseSpecialFloatingPointValues() throws ParseException {
        String json = "{\"nan\": \"NaN\", \"infinity\": \"Infinity\"}";
        Object result = parser.parse(json);
        assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("NaN", resultMap.get("nan"));
        assertEquals("Infinity", resultMap.get("infinity"));
    }

    @Test
    void unexpectedEndOfJson() {
        String json = "{\"key\": 42";
        assertThrows(ParseException.class, () -> parser.parse(json));
    }

    @Test
    void parseDeeplyNestedJson() throws ParseException {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":\"deep\"}}}}}}";
        Object result = parser.parse(json);
        assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        Map<?, ?> aMap = (Map<?, ?>) resultMap.get("a");
        Map<?, ?> bMap = (Map<?, ?>) aMap.get("b");
        Map<?, ?> cMap = (Map<?, ?>) bMap.get("c");
        Map<?, ?> dMap = (Map<?, ?>) cMap.get("d");
        Map<?, ?> eMap = (Map<?, ?>) dMap.get("e");
        assertEquals("deep", eMap.get("f"));
    }
}
