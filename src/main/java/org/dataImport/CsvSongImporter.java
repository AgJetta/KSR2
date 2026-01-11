package org.dataImport;

import java.io.*;
import java.util.*;
import org.fuzzy.SongRecord;

public class CsvSongImporter {

    // Hardcoded headers in exact order as they appear in the CSV file
    private static final List<String> HEADERS = Arrays.asList(
            "playlist_genre", "track_popularity", "danceability", "energy",
            "loudness", "acousticness", "instrumentalness", "liveness", "valence",
            "tempo", "duration_ms"
    );

    // Only numeric fields that go into SongRecord
    private static final List<String> NUMERIC_FIELDS = HEADERS.subList(1, HEADERS.size());

    public static List<SongRecord> importSongs(int maxRows) {
        List<SongRecord> songs = new ArrayList<>();
        try {
            InputStream inputStream = CsvSongImporter.class.getClassLoader()
                    .getResourceAsStream("org/dataLoader/data_genre.csv");

            if (inputStream == null) {
                throw new IOException("Could not find data.csv in resources/org/dataLoader/");
            }


            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                // Skip header line
                reader.readLine();

                String line;
                int rowCount = 0;

                while ((line = reader.readLine()) != null && rowCount < maxRows) {
                    String[] values = line.split("\\s+");

                    Map<String, Double> attributes = new LinkedHashMap<>();

                    // Map values to numeric fields maintaining order
                    for (int i = 0; i < HEADERS.size() && i < values.length; i++) {
                        String fieldName = HEADERS.get(i);
                        if (i == 0) {
                            // Special case for playlist_genre, convert to double
                            Double genreValue = SongRecord.genreStringtoDouble(values[i]);
                            if (genreValue == null) {
                                System.err.println("Invalid genre value: " + values[i]);
                                System.exit(1);
                            }
                            attributes.put(fieldName, genreValue);
                        }
                        if (NUMERIC_FIELDS.contains(fieldName)) {
                            try {
                                double numericValue = Double.parseDouble(values[i]);
                                // Adjust loudness to be in [0, 61.125] instead of [-60, 1.125]
                                if (fieldName.equals("loudness")) {
                                    numericValue += 60;
                                }
                                attributes.put(fieldName, numericValue);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid number format for field '" + fieldName + "': " + values[i]);
                                System.exit(1);
                            }
                        }
                    }

                    songs.add(new SongRecord(attributes));
                    rowCount++;
                }
            }

            return songs;
        } catch (IOException e) {
            System.err.println("Couldn't load the dataset: " + e.getMessage());
            System.exit(1);
            return songs;
        }
    }

    public static void main(String[] args) {
        try {
            List<SongRecord> songs = importSongs(10000);

            System.out.println("Imported " + songs.size() + " songs:");
            System.out.println();
            System.out.println(songs.get(0).attributes().keySet());

            for (SongRecord song : songs) {
                for (String fieldName : NUMERIC_FIELDS) {
                    System.out.printf("%.2f ", song.getAttribute(fieldName));
                }
                System.out.println();

                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }
}