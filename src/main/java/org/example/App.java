package org.example;

import java.util.Map;

public class App
{
    public static void main(String[] args) {
        JsonParser parser = new JsonParser();
        try {
            String json = "{ \"name\": \"John Doe\", \"occupation\": \"Developer\", \"bio\": \"John is a \\\"senior\\\" developer with 10 years of experience. He lives in San Francisco and loves coding in Java. His favorite escape sequence is \\\\\\ for backslashes.\" }";

            Map<String, Object> parsed = (Map<String, Object>) parser.parse(json);
            String pretty = parser.prettyPrint(parsed);
            System.out.println(pretty);
        } catch (JsonParser.ParseException e) {
            System.err.println(e.getMessage());
        }
    }
}
