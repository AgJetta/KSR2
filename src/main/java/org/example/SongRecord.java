package org.example;

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
}
