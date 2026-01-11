package org.dataImport;

import org.fuzzy.SongRecord;
import java.util.*;

public class TestDataGenerator {
    
    public static List<SongRecord> generateTestData(int count) {
        List<SongRecord> songs = new ArrayList<>();
        Random random = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < count; i++) {
            Map<String, Double> attributes = new LinkedHashMap<>();
            
            // Genre (0-5: pop, rock, rap, edm, r&b, latin)
            attributes.put("playlist_genre", (double) random.nextInt(6));
            
            // Track popularity (0-100)
            attributes.put("track_popularity", random.nextDouble() * 100);
            
            // Danceability (0.0-1.0)
            attributes.put("danceability", random.nextDouble());
            
            // Energy (0.0-1.0)
            attributes.put("energy", random.nextDouble());
            
            // Loudness (0-61.275, adjusted from -60 to 1.125)
            attributes.put("loudness", random.nextDouble() * 61.275);
            
            // Acousticness (0.0-1.0)
            attributes.put("acousticness", random.nextDouble());
            
            // Instrumentalness (0.0-1.0)
            attributes.put("instrumentalness", random.nextDouble());
            
            // Liveness (0.0-1.0)
            attributes.put("liveness", random.nextDouble());
            
            // Valence (0.0-1.0)
            attributes.put("valence", random.nextDouble());
            
            // Tempo (50-250 BPM)
            attributes.put("tempo", 50 + random.nextDouble() * 200);
            
            // Duration in milliseconds (30s to 8min)
            attributes.put("duration_ms", 30000 + random.nextDouble() * 450000);
            
            songs.add(new SongRecord(attributes));
        }
        
        return songs;
    }
}
