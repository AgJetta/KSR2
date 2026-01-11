package org.fuzzy;

import java.util.Map;
import java.util.Set;

// Dataset record structure
public record SongRecord(Map<String, Double> attributes) {
    public double getAttribute(String fieldName) {
        return attributes.getOrDefault(fieldName, 0.0);
    }

    public Set<String> getFieldNames() {
        return attributes.keySet();
    }

    public static Double genreStringtoDouble(String genre) {
        return switch (genre) {
            case "pop" -> 0.0;
            case "rock" -> 1.0;
            case "rap" -> 2.0;
            case "edm" -> 3.0;
            case "r&b" -> 4.0;
            case "latin" -> 5.0;
            default -> null;
        };
    }

    public static String genreDoubleToString(Double genre) {
        return switch (genre.intValue()) {
            case 0 -> "pop";
            case 1 -> "rock";
            case 2 -> "rap";
            case 3 -> "edm";
            case 4 -> "r&b";
            case 5 -> "latin";
            default -> null;
        };
    }
}
