package org.dataImport;

import org.fuzzy.LogicalConnective;
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
import java.util.Arrays;
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

            // Access quantifiers array
            JSONArray json_quantifiers = config.getJSONArray("quantifiers");
            for (int i = 0; i < json_quantifiers.length(); i++) {
                JSONObject quantifierObj = json_quantifiers.getJSONObject(i);

                // Extract basic properties
                String quantifierName = quantifierObj.getString("name");
                String functionType = quantifierObj.getString("functionType");
                JSONArray parametersArray = quantifierObj.getJSONArray("parameters");
                boolean isRelative = quantifierObj.getBoolean("relative");

                // Convert JSONArray of parameters to double[]
                double[] parameters = new double[parametersArray.length()];
                for (int j = 0; j < parametersArray.length(); j++) {
                    parameters[j] = parametersArray.getDouble(j);
                }

                // Extract universe
                JSONArray universeArray = quantifierObj.getJSONArray("universe");
                double universeMin = universeArray.getDouble(0);
                double universeMax = universeArray.getDouble(1);
                Universe universe = new Universe(universeMin, universeMax, isRelative);

                // Create membership function
                MembershipFunction membershipFunction = createMembershipFunction(functionType, parametersArray);

                // Create fuzzy set
                FuzzySet fuzzySet = new FuzzySet(universe, membershipFunction);

                // Optional: clamp values for display (not used for calculation)
                double quantifierMinValue = parameters[0];
                double quantifierMaxValue = parameters[parameters.length - 1];
                if (quantifierMinValue < 0.0) quantifierMinValue = 0.0;
                if (isRelative && quantifierMaxValue > 1.0) quantifierMaxValue = 1.0;
                if (quantifierMaxValue > universeMax) quantifierMaxValue = universeMax;

                // Create quantifier with new constructor (includes functionType and parameters)
                Quantifier q = new Quantifier(
                        quantifierName,
                        fuzzySet,
                        isRelative,
                        functionType,
                        parameters
                );

                quantifiers.add(q);
            }

            return quantifiers;
        } catch (Exception e) {
            System.err.println("Error loading quantifiers: " + e.getMessage());
            return quantifiers;
        }
    }

    public static List<Summarizer> loadSummarizersFromConfig() {
        List<Summarizer> summarizers = new ArrayList<>();

        try {
            JSONObject config = loadConfig();

            JSONArray variables = config.getJSONArray("variables");

            for (int i = 0; i < variables.length(); i++) {
                JSONObject variable = variables.getJSONObject(i);

                String variableName = variable.getString("name");          // e.g. "tempo"
                String fieldName = variable.getString("fieldName");        // e.g. "tempo"
                JSONArray universeArray = variable.getJSONArray("universe");

                double uMin = universeArray.getDouble(0);
                double uMax = universeArray.getDouble(1);
                Universe universe = new Universe(uMin, uMax, true);

                JSONArray terms = variable.getJSONArray("terms");
                for (int j = 0; j < terms.length(); j++) {
                    JSONObject term = terms.getJSONObject(j);

                    String termName = term.getString("name");              // e.g. "fast"
                    String functionType = term.getString("functionType");

                    JSONArray paramArray = term.getJSONArray("parameters");
                    double[] params = new double[paramArray.length()];
                    for (int k = 0; k < paramArray.length(); k++) {
                        params[k] = paramArray.getDouble(k);
                    }

                    // Create membership function
                    MembershipFunction mf = createMembershipFunction(functionType, paramArray);

                    // Create fuzzy set
                    FuzzySet fuzzySet = new FuzzySet(universe, mf);

                    // ✅ CORRECT constructor
                    Summarizer summarizer = new Summarizer(
                            termName,          // summarizer name
                            fieldName,         // DB field
                            fuzzySet,
                            functionType,      // metadata for GUI
                            params,
                            universe
                    );

                    summarizers.add(summarizer);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return summarizers;
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

    /**
     * Creates compound summarizers by combining simple ones with logical connectives.
     *
     * @param simpleSummarizers List of simple summarizers loaded from config
     * @param combinations Each int[] specifies indices of summarizers to combine
     * @param connectives Each LogicalConnective[] specifies connectives between components
     * @return List of compound summarizers
     */
    public static List<Summarizer> createCompoundSummarizers(
            List<Summarizer> simpleSummarizers,
            List<int[]> combinations,
            List<LogicalConnective[]> connectives) {

        if (combinations.size() != connectives.size()) {
            throw new IllegalArgumentException(
                    "Number of combinations must match number of connective arrays");
        }

        List<Summarizer> compounds = new ArrayList<>();

        for (int i = 0; i < combinations.size(); i++) {
            int[] indices = combinations.get(i);
            LogicalConnective[] conns = connectives.get(i);

            if (indices.length != conns.length + 1) {
                throw new IllegalArgumentException(
                        "Number of connectives must be one less than number of components");
            }

            // Gather components
            List<String> fieldNames = new ArrayList<>();
            List<FuzzySet> fuzzySets = new ArrayList<>();
            List<String> lingVars = new ArrayList<>();

            for (int idx : indices) {
                if (idx < 0 || idx >= simpleSummarizers.size()) {
                    throw new IndexOutOfBoundsException(
                            "Summarizer index " + idx + " out of bounds");
                }

                Summarizer s = simpleSummarizers.get(idx);

                // Simple summarizers have single components at index 0
                fieldNames.add(s.getFieldName(0));
                fuzzySets.add(s.getFuzzySet(0));
                lingVars.add(s.getLinguisticVariable(0));
            }

            // Build name
            String name = buildCompoundName(simpleSummarizers, indices, conns);

            // Create compound summarizer
            List<LogicalConnective> connList = Arrays.asList(conns);
            Summarizer compound = new Summarizer(name, fieldNames, fuzzySets, connList, lingVars);
            compounds.add(compound);
        }

        return compounds;
    }

    private static String buildCompoundName(List<Summarizer> summarizers,
                                            int[] indices,
                                            LogicalConnective[] connectives) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indices.length; i++) {
            sb.append(summarizers.get(indices[i]).getName());
            if (i < connectives.length) {
                sb.append(" ").append(connectives[i].name()).append(" ");
            }
        }
        return sb.toString();
    }


    public static void main(String[] args) throws Exception {
        // Load and create summarizers
        List<Summarizer> summarizers = loadSummarizersFromConfig();

        System.out.println("\n=== Created Summarizers ===");
        for (Summarizer summarizer : summarizers) {
            System.out.println("Summarizer: " + summarizer.getName() +
                    " (field: " + summarizer.getFieldName(1) + ")");
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