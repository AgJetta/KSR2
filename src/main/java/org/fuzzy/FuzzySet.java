package org.fuzzy;

import org.fuzzy.membershipFunctions.MembershipFunction;
import org.fuzzy.membershipFunctions.MembershipFunctions;

import java.util.*;
import java.util.stream.Collectors;


// Main FuzzySet class
public class FuzzySet {
    private final Universe universe;
    private final Map<Double, Double> memberships;
    private final MembershipFunction membershipFunction;
    private boolean isClassic = false; // Flag to indicate if this is a classic fuzzy set
    private List<SongRecord> data = null; // List of song records for this fuzzy set
    private String fieldName = null; // Field name for the fuzzy set, used for connecting to datasets

    private double mfBeginning = 0.0;

    private double mfEnd = 0.0;

    // Constructor for fuzzy set with explicit memberships
    public FuzzySet(Universe universe, MembershipFunction membershipFunction, Map<Double, Double> memberships) {
        this.universe = universe;
        this.membershipFunction = membershipFunction;
        this.memberships = new HashMap<>();

        // Validate and store memberships
        for (Map.Entry<Double, Double> entry : memberships.entrySet()) {
            double x = entry.getKey();
            double membership = entry.getValue();

            if (membership < 0.0 || membership > 1.0) {
                throw new IllegalArgumentException("Membership must be in [0, 1]");
            }

            this.memberships.put(x, membership);
        }
    }

    // Constructor with membership function
    public FuzzySet(Universe universe, MembershipFunction function) {
        this.universe = universe;
        this.memberships = new HashMap<>();
        this.membershipFunction = function;

//        if (universe.isDense()) {
//            // For dense universe, we need to sample points
//            double step = universe.getCardinalNumber() / 1000.0; // Sample 1000 points
//            for (double x = universe.getStart(); x <= universe.getEnd(); x += step) {
//                double membership = this.membershipFunction.apply(x);
//                if (membership > 0.0) { // Only store non-zero memberships
//                    this.memberships.put(x, membership);
//                }
//            }
//        } else {
//            // For discrete universe, evaluate at all points
//            for (double x : universe.getDiscretePoints()) {
//                double membership = this.membershipFunction.apply(x);
//                if (membership > 0.0) {
//                    this.memberships.put(x, membership);
//                }
//            }
//        }
    }

    public void connectDataset(List<SongRecord> data, String fieldName) {
        this.data = data;
        if (Objects.equals(fieldName, "duration_ms")) {
            fieldName = "duration_ms"; // Special case for duration_ms field
        }
        if (data != null) {
            for (SongRecord record : data) {
                double fieldValue = record.getAttribute(fieldName);
                double membership = this.membershipFunction.apply(fieldValue);
                if (membership > 0.0) {
                    memberships.put(fieldValue, membership);
                }
            }
        }
    }

    public MembershipFunction getMembershipFunction() {
        return membershipFunction;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    /** Factory methods for classic sets
     * Creates a classic fuzzy set with full membership in the range [start, end]
     * @param universe of Discourse
     * @param start Values bigger than or equal to this will have full membership
     * @param end Values smaller than or equal to this will have full membership
     * @return FuzzySet with full membership in the specified range and crisp membership function
     */
    public static FuzzySet classicSet(Universe universe, double start, double end) {
        FuzzySet returnSet = new FuzzySet(universe, MembershipFunctions.crisp(start, end));
        returnSet.setClassic(true); // Mark as classic set
        return returnSet;
    }

    /** Factory method for classic fuzzy set with specific elements
     * Creates a classic fuzzy set with full membership for specified elements
     * @param universe of Discourse
     * @param start Values bigger than or equal to this will have full membership
     * @param end Values smaller than or equal to this will have full membership
     * @param elements Set of elements with full membership
     * @return FuzzySet with full membership for specified elements and crisp membership function
     */
    public static FuzzySet classicSet(Universe universe, double start, double end, Set<Double> elements) {

        Map<Double, Double> memberships = new HashMap<>();
        for (double x : elements) {
            memberships.put(x, 1.0);
        }
        FuzzySet returnSet = new FuzzySet(universe, MembershipFunctions.crisp(start, end), memberships);
        returnSet.setClassic(true); // Mark as classic set
        return returnSet;
    }

    // Basic operations
    public double getMembership(double x) {
        double membership = memberships.getOrDefault(x, 0.);
        return membership;
    }

    public Set<Tuple> getTuples() {
        return memberships.entrySet().stream()
                .map(entry -> new Tuple(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    public Universe getUniverse() {
        return universe;
    }

    // Set properties
    public boolean isEmpty() {
        return memberships.isEmpty() || memberships.values().stream().allMatch(m -> m == 0.0);
    }

    public boolean isNormal() {
        return memberships.values().stream().anyMatch(m -> m == 1.0);
    }

    public double height() {
        return memberships.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    public boolean isConvex() {
        if (memberships.size() < 3) return true;

        List<Double> sortedKeys = memberships.keySet().stream().sorted().toList();

        for (int i = 1; i < sortedKeys.size() - 1; i++) {
            double x1 = sortedKeys.get(i - 1);
            double x2 = sortedKeys.get(i);
            double x3 = sortedKeys.get(i + 1);

            double lambda = (x2 - x1) / (x3 - x1);
            double expectedMembership = lambda * getMembership(x3) + (1 - lambda) * getMembership(x1);

            if (getMembership(x2) < expectedMembership) {
                return false;
            }
        }
        return true;
    }

    // Set operations
    public FuzzySet complement() {
        Map<Double, Double> newMemberships = new HashMap<>();

        if (universe.isDense()) {
            // For dense universe, complement of stored points
            for (Map.Entry<Double, Double> entry : memberships.entrySet()) {
                newMemberships.put(entry.getKey(), 1.0 - entry.getValue());
            }
        } else {
            // For discrete universe, evaluate complement for all points
            for (double x : universe.getDiscretePoints()) {
                newMemberships.put(x, 1.0 - getMembership(x));
            }
        }

        return new FuzzySet(universe, membershipFunction, newMemberships);
    }

    public FuzzySet union(FuzzySet other) {
        if (!this.universe.equals(other.universe)) {
            throw new IllegalArgumentException("Sets must have the same universe");
        }

        Map<Double, Double> newMemberships = new HashMap<>();
        Set<Double> allKeys = new HashSet<>(this.memberships.keySet());
        allKeys.addAll(other.memberships.keySet());

        for (double x : allKeys) {
            double membership = Math.max(this.getMembership(x), other.getMembership(x));
            if (membership > 0.0) {
                newMemberships.put(x, membership);
            }
        }

        return new FuzzySet(universe, membershipFunction, newMemberships);
    }

    public FuzzySet intersection(FuzzySet other) {

        Map<Double, Double> newMemberships = new HashMap<>();
        FuzzySet smallerSet = this.memberships.size() < other.memberships.size() ? this : other;
        FuzzySet largerSet = this.memberships.size() < other.memberships.size() ? other : this;
//        for (SongRecord record : this.data) {
//            double thisSetElement = record.getAttribute(this.fieldName);
//            double otherSetElement = record.getAttribute(other.fieldName);
//            double thisMembership = this.getMembership(thisSetElement);
//            double otherMembership = other.getMembership(otherSetElement);
//            double membership = Math.min(thisMembership, otherMembership);
//            if (membership > 0.0) {
//                newMemberships.put(thisSetElement, membership);
//            }
//        }

        for (SongRecord record : this.data) {
            double smallerSetElement = record.getAttribute(smallerSet.fieldName);
            if(smallerSet.getMembership(smallerSetElement) == 0) {
                continue; // Skip if smaller set has no membership for this element
            }
            double largerSetElement = record.getAttribute(largerSet.fieldName);
            double smallerMembership = smallerSet.getMembership(smallerSetElement);
            double largerMembership = largerSet.getMembership(largerSetElement);
            double membership = Math.min(smallerMembership, largerMembership);
            if (membership > 0.0) {
                newMemberships.put(smallerSetElement, membership);
            }
        }

        return new FuzzySet(universe, membershipFunction, newMemberships);
    }

    // Additional methods for linguistic summaries

    // Alpha-cut: returns crisp set of elements with membership >= alpha
    public FuzzySet alphaCut(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Alpha must be in [0, 1]");
        }

        Set<Double> alphaMemberships = memberships.entrySet().stream()
                .filter(entry -> entry.getValue() > alpha)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        double minVal = universe.getStart();
        double maxVal = universe.getEnd();
        return FuzzySet.classicSet(universe, minVal, maxVal, alphaMemberships);
    }

    // Support: elements with membership > 0
    public FuzzySet support() {
        return alphaCut(Double.MIN_VALUE);
    }

    // Core: elements with membership = 1
    public FuzzySet core() {
        return alphaCut(1.0);
    }

    // Cardinality (sigma-count)
    public double cardinalNumber() {
        return memberships.values().stream().mapToDouble(Double::doubleValue).sum();
//        if (!isClassic) {
//            return memberships.values().stream().mapToDouble(Double::doubleValue).sum();
//        } else {
//            if (memberships.isEmpty()) {
//               return 0.0; // Classic set cardinality is zero if no memberships
//            }
//
//            double minValueInMemberships = memberships.keySet().stream()
//                    .min(Double::compareTo)
//                    .orElseThrow(() -> new IllegalStateException("No elements in classic set"));
//            double maxValueInMemberships = memberships.keySet().stream()
//                    .max(Double::compareTo)
//                    .orElseThrow(() -> new IllegalStateException("No elements in classic set"));
//            return maxValueInMemberships - minValueInMemberships; // Classic set cardinality
//        }
    }

    // Centroid defuzzification
    public double centroid() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot compute centroid of empty set");
        }

        double numerator = memberships.entrySet().stream()
                .mapToDouble(entry -> entry.getKey() * entry.getValue())
                .sum();

        double denominator = memberships.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return numerator / denominator;
    }

    // Placeholder methods for future T1-T11 measures
    public double degreeOfFuzziness() {
        double supportCardinality = support().cardinalNumber();
        double universeCardinalNumber = universe.getCardinalNumber();
        return (supportCardinality / universeCardinalNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FuzzySet other)) return false;

        return universe.equals(other.universe) && memberships.equals(other.memberships);
    }

    @Override
    public int hashCode() {
        return Objects.hash(universe, memberships);
    }

    @Override
    public String toString() {
        return "FuzzySet{" +
                "universe=" + universe +
                ", memberships=" + memberships +
                '}';
    }

    public boolean isClassic() {
        return isClassic;
    }

    public void setClassic(boolean classic) {
        isClassic = classic;
    }
}
