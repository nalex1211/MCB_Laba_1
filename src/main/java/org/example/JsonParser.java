package org.example;
import java.math.BigDecimal;
import java.util.*;

public class JsonParser {

    public Object parse(String json) throws ParseException {
        json = json.trim();

        if (json.startsWith("{")) {
            return parseObject(json);
        } else if (json.startsWith("[")) {
            return parseArray(json);
        } else if (json.startsWith("\"")) {
            return parseString(json);
        } else {
            throw new ParseException("Invalid JSON string at position 0");
        }
    }

    private String parseString(String json) throws ParseException {
        if (json.endsWith("\"") && json.length() > 1) {
            json = json.substring(1, json.length() - 1);
            json = json.replaceAll("\\\\\"", "\"")
                    .replaceAll("\\\\n", "\n")
                    .replaceAll("\\\\t", "\t")
                    .replaceAll("\\\\b", "\b")
                    .replaceAll("\\\\r", "\r")
                    .replaceAll("\\\\f", "\f")
                    .replaceAll("\\\\\\\\", "\\\\");

            return json;
        } else {
            throw new ParseException("Invalid JSON string");
        }
    }

    private Map<String, Object> parseObject(String json) throws ParseException {
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new ParseException("Invalid JSON object at position 0");
        }

        json = json.substring(1, json.length() - 1).trim();
        Map<String, Object> map = new LinkedHashMap<>();

        while (!json.isEmpty()) {
            int keyStart = json.indexOf('\"');
            int keyEnd = json.indexOf("\"", keyStart + 1);
            if (keyStart != 0 || keyEnd == -1) {
                throw new ParseException("Invalid JSON string");
            }

            String key = json.substring(keyStart + 1, keyEnd);
            json = json.substring(keyEnd + 1).trim();

            if (!json.startsWith(":")) {
                throw new ParseException("Invalid JSON object structure, expecting ':' after key");
            }
            json = json.substring(1).trim();

            int valueEnd = findValueEnd(json);
            if (valueEnd == -1) {
                throw new ParseException("Invalid JSON value structure");
            }

            String valueStr = json.substring(0, valueEnd);
            Object value = parseValue(valueStr.trim());
            map.put(key, value);

            json = json.substring(valueEnd).trim();
            if (!json.isEmpty() && json.startsWith(",")) {
                json = json.substring(1).trim();
            }
        }

        return map;
    }

    private int findValueEnd(String json) {
        int depth = 0;
        boolean inString = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '\"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{' || c == '[') {
                    depth++;
                } else if (c == '}' || c == ']') {
                    depth--;
                } else if (depth == 0 && c == ',') {
                    return i;
                }
            }
        }

        return json.length();
    }

    private List<Object> parseArray(String json) throws ParseException {
        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new ParseException("Invalid JSON array at position 0");
        }

        List<Object> list = new ArrayList<>();
        json = json.trim().substring(1, json.length() - 1).trim();

        if (json.isEmpty()) {
            return list;
        }

        while (!json.isEmpty()) {
            int valueEnd = findValueEnd(json);
            if (valueEnd == -1) {
                throw new ParseException("Invalid JSON value structure");
            }

            String valueStr = json.substring(0, valueEnd);
            Object value = parseValue(valueStr.trim());
            list.add(value);

            json = json.substring(valueEnd).trim();
            if (!json.isEmpty() && json.startsWith(",")) {
                json = json.substring(1).trim();
            }
        }

        return list;
    }


    private Object parseValue(String json) throws ParseException {
        json = json.trim();
        if (json.startsWith("\"")) {
            return parseString(json);
        } else if (json.startsWith("{")) {
            return parseObject(json);
        } else if (json.startsWith("[")) {
            return parseArray(json);
        } else if (json.matches("-?\\d+(\\.\\d+)?")) {
            return new BigDecimal(json);
        } else if (json.equalsIgnoreCase("true") || json.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(json);
        } else if (json.equals("null")) {
            return null;
        } else {
            throw new ParseException("Unknown type at position " + json.indexOf(json));
        }
    }

    private List<String> splitJson(String json, char delimiter) {
        List<String> list = new ArrayList<>();
        int bracketCount = 0;
        int start = 0;
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == delimiter && bracketCount == 0 && !inString) {
                list.add(json.substring(start, i));
                start = i + 1;
            } else if (ch == '{' || ch == '[') {
                bracketCount++;
            } else if (ch == '}' || ch == ']') {
                bracketCount--;
            } else if (ch == '\"') {
                inString = !inString;
            }
        }
        list.add(json.substring(start));
        return list;
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    public String prettyPrint(Map<String, Object> jsonMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return builder.toString();
    }
}
