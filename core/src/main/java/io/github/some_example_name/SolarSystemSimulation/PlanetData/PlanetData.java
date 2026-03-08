package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import java.util.Map;

// POJO that mirrors one entry in planets.json — Gson populates this via reflection so field names must match the JSON keys exactly
public class PlanetData {

    private String name;
    private String description;

    // Array of structured fact objects rather than plain strings — each fact has a title and body text
    private Fact[] facts;

    // Key-value pairs of stat labels to stat values (e.g. "Mass" → "1.898 × 10²⁷ kg")
    private Map<String, String> stats;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Fact[] getFacts() { return facts; }
    public Map<String,String> getStats() { return stats; }

    // Inner class representing a single fact card — title is displayed as a heading, text as the body
    public static class Fact {

        private String title;
        private String text;

        public String getTitle() { return title; }
        public String getText() { return text; }
    }
}
