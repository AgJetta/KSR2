package org.dataImport;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConfigImporter {
    public static void main(String[] args) throws Exception {
        // Load config.json from resources
        InputStream input = ConfigImporter.class.getClassLoader().getResourceAsStream("org/dataLoader/config.json");
        assert input != null;
        String jsonText = new Scanner(input, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        JSONObject config = new JSONObject(jsonText);

        // Access function types
        JSONArray functionTypes = config.getJSONArray("functionTypes");
        for (int i = 0; i < functionTypes.length(); i++) {
            System.out.println("Function type: " + functionTypes.getString(i));
        }

        // Access quantifiers
        JSONArray quantifiers = config.getJSONArray("quantifiers");
        for (int i = 0; i < quantifiers.length(); i++) {
            JSONObject q = quantifiers.getJSONObject(i);
            System.out.println("Quantifier name: " + q.getString("name"));
            System.out.println("  Type: " + q.getString("functionType"));
            System.out.println("  Params: " + q.getJSONArray("parameters"));
        }

        // Access variables and terms
        JSONArray variables = config.getJSONArray("variables");
        for (int i = 0; i < variables.length(); i++) {
            JSONObject variable = variables.getJSONObject(i);
            System.out.println("Variable: " + variable.getString("name"));

            JSONArray terms = variable.getJSONArray("terms");
            for (int j = 0; j < terms.length(); j++) {
                JSONObject term = terms.getJSONObject(j);
                System.out.println("  Term: " + term.getString("name"));
                System.out.println("    Function: " + term.getString("functionType"));
                System.out.println("    Parameters: " + term.getJSONArray("parameters"));
            }
        }
    }
}

