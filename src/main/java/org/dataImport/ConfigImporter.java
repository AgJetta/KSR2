package org.dataImport;

import org.fuzzy.quantifiers.Quantifier;
import org.json.JSONObject;
import org.json.JSONArray;
import org.fuzzy.FuzzySet;
import org.fuzzy.Universe;
import org.fuzzy.membershipFunctions.MembershipFunction;
import org.fuzzy.membershipFunctions.MembershipFunctions;
import org.fuzzy.summarizer.Summarizer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConfigImporter {

    private static JSONObject loadConfig() {
        // Load config.json from resources
        InputStream input = ConfigImporter.class.getClassLoader().getResourceAsStream("org/dataLoader/config.json");
        assert input != null;
        String jsonText = new Scanner(input, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        JSONObject config = new JSONObject(jsonText);
        return config;
    }

    public static List<Quantifier> loadQuantifiersFromConfig() {
        List<Quantifier> quantifiers = new ArrayList<>();

        try {
            JSONObject config = loadConfig();

            // Access variables and terms
            JSONArray json_quantifiers = config.getJSONArray("quantifiers");
            for (int i = 0; i < json_quantifiers.length(); i++) {
                JSONObject quantifier = json_quantifiers.getJSONObject(i);
                JSONArray universeArray = quantifier.getJSONArray("universe");


                // Extract term properties
                String quantifierName = quantifier.getString("name");
                String functionType = quantifier.getString("functionType");
                JSONArray parametersArray = quantifier.getJSONArray("parameters");
                boolean isRelative = quantifier.getBoolean("relative");

                // Create universe
                double universeMin = universeArray.getDouble(0);
                double universeMax = universeArray.getDouble(1);
                Universe universe = new Universe(universeMin, universeMax, true);

                // Create membership function based on type
                MembershipFunction membershipFunction = createMembershipFunction(functionType, parametersArray);

                // Create fuzzy set and summarizer
                FuzzySet fuzzySet = new FuzzySet(universe, membershipFunction);
                Quantifier return_quantifier = new Quantifier(quantifierName, fuzzySet, isRelative);
                quantifiers.add(return_quantifier);

//                System.out.println("Created quantifier: " + quantifierName);
            }

            return quantifiers;
        } catch (Exception e) {
            System.err.println("Error in loading quantifiers, none loaded: " + e.getMessage());
            return quantifiers;
        }
    }

    public static List<Summarizer> loadSummarizersFromConfig() {
        List<Summarizer> summarizers = new ArrayList<>();

        try {
            JSONObject config = loadConfig();

            // Access variables and terms
            JSONArray variables = config.getJSONArray("variables");
            for (int i = 0; i < variables.length(); i++) {
                JSONObject variable = variables.getJSONObject(i);
                String variableName = variable.getString("name");
                String variableDatabaseName = variable.getString("fieldName");
                JSONArray universeArray = variable.getJSONArray("universe");

                JSONArray terms = variable.getJSONArray("terms");
                for (int j = 0; j < terms.length(); j++) {
                    JSONObject term = terms.getJSONObject(j);

                    // Extract term properties
                    String termName = term.getString("name");
                    String functionType = term.getString("functionType");
                    JSONArray parametersArray = term.getJSONArray("parameters");

                    // Create universe
                    double universeMin = universeArray.getDouble(0);
                    double universeMax = universeArray.getDouble(1);
                    Universe universe = new Universe(universeMin, universeMax, true);

                    // Create membership function based on type
                    MembershipFunction membershipFunction = createMembershipFunction(functionType, parametersArray);

                    // Create fuzzy set and summarizer
                    FuzzySet fuzzySet = new FuzzySet(universe, membershipFunction);
                    Summarizer summarizer = new Summarizer(termName, variableDatabaseName, fuzzySet);
                    summarizer.linguisiticVariable = variableName;
                    summarizers.add(summarizer);

//                    System.out.println("Created summarizer: " + termName + " for field: " + variableDatabaseName);
                }
            }

            return summarizers;
        } catch (Exception e) {
            System.err.println("Error in loading summarizers, none loaded: " + e.getMessage());
            return summarizers;
        }
    }

    private static MembershipFunction createMembershipFunction(String functionType, JSONArray parameters) {
        switch (functionType.toLowerCase()) {
            case "triangular":
                if (parameters.length() != 3) {
                    throw new IllegalArgumentException("Triangular function requires exactly 3 parameters");
                }
                return MembershipFunctions.triangular(
                        parameters.getDouble(0),
                        parameters.getDouble(1),
                        parameters.getDouble(2)
                );

            case "trapezoidal":
                if (parameters.length() != 4) {
                    throw new IllegalArgumentException("Trapezoidal function requires exactly 4 parameters");
                }
                return MembershipFunctions.trapezoidal(
                        parameters.getDouble(0),
                        parameters.getDouble(1),
                        parameters.getDouble(2),
                        parameters.getDouble(3)
                );

            case "gaussian":
                if (parameters.length() != 2) {
                    throw new IllegalArgumentException("Gaussian function requires exactly 2 parameters (mean, stddev)");
                }
                return MembershipFunctions.gaussian(
                        parameters.getDouble(0),
                        parameters.getDouble(1)
                );

            case "crisp":
                if (parameters.length() != 2) {
                    throw new IllegalArgumentException("Crisp function requires exactly 2 parameters");
                }
                return MembershipFunctions.crisp(
                        parameters.getDouble(0),
                        parameters.getDouble(1)
                );

            case "rampdown":
                if (parameters.length() != 2) {
                    throw new IllegalArgumentException("RampDown function requires exactly 2 parameters");
                }
                return MembershipFunctions.rampDown(
                        parameters.getDouble(0),
                        parameters.getDouble(1)
                );

            case "rampup":
                if (parameters.length() != 2) {
                    throw new IllegalArgumentException("RampUp function requires exactly 2 parameters");
                }
                return MembershipFunctions.rampUp(
                        parameters.getDouble(0),
                        parameters.getDouble(1)
                );

            default:
                throw new IllegalArgumentException("Unknown function type: " + functionType);
        }
    }

    public static void main(String[] args) throws Exception {
        // Load and create summarizers
        List<Summarizer> summarizers = loadSummarizersFromConfig();

        System.out.println("\n=== Created Summarizers ===");
        for (Summarizer summarizer : summarizers) {
            System.out.println("Summarizer: " + summarizer.getName() +
                    " (field: " + summarizer.getFieldName() + ")");
        }

        // Original config reading code for reference
        System.out.println("\n=== Original Config Analysis ===");
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
    }
}