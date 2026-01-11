package org.fuzzy;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.fuzzy.membershipFunctions.MembershipFunction;
import org.fuzzy.membershipFunctions.MembershipFunctions;

import java.util.*;
import java.util.stream.Collectors;

public class FuzzySet {
    private final Universe universe;
    private final MembershipFunction membershipFunction;
    private final Map<Double, Double> membershipCache;
    private boolean isClassic = false;

    public FuzzySet(Universe universe, MembershipFunction membershipFunction, Map<Double, Double> memberships) {
        this.universe = universe;
        this.membershipFunction = membershipFunction;
        this.membershipCache = new HashMap<>();

        for (Map.Entry<Double, Double> entry : memberships.entrySet()) {
            double x = entry.getKey();
            double membership = entry.getValue();

            if (membership < 0.0 || membership > 1.0) {
                throw new IllegalArgumentException("Membership must be in [0, 1]");
            }

            this.membershipCache.put(x, membership);
        }
    }

    public FuzzySet(Universe universe, MembershipFunction function) {
        this.universe = universe;
        this.membershipFunction = function;
        this.membershipCache = new HashMap<>();
    }

    public double getMembership(double x) {
        if (isClassic && !universe.isDense()) {
            return membershipCache.getOrDefault(x, 0.0);
        }

        if (membershipCache.containsKey(x)) {
            return membershipCache.get(x);
        }

        if (membershipFunction != null) {
            double membership = membershipFunction.apply(x);
            membershipCache.put(x, membership);
            return membership;
        }

        return 0.0;
    }

    public void cacheMembershipsForPoints(Set<Double> points) {
        for (double x : points) {
            if (!membershipCache.containsKey(x)) {
                double membership = membershipFunction.apply(x);
                if (membership > 0.0) {
                    membershipCache.put(x, membership);
                }
            }
        }
    }

    public MembershipFunction getMembershipFunction() {
        return membershipFunction;
    }

    public Set<Tuple> getTuples() {
        return membershipCache.entrySet().stream()
                .map(entry -> new Tuple(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    public Universe getUniverse() {
        return universe;
    }

    public boolean isEmpty() {
        return membershipCache.isEmpty() || membershipCache.values().stream().allMatch(m -> m == 0.0);
    }

    public boolean isNormal() {
        return membershipCache.values().stream().anyMatch(m -> m == 1.0);
    }

    public double height() {
        return membershipCache.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    public boolean isConvex() {
        if (membershipCache.size() < 3) return true;

        List<Double> sortedKeys = membershipCache.keySet().stream().sorted().toList();

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

    public FuzzySet complement() {
        if (universe.isDense()) {
            MembershipFunction complementFunc = x -> 1.0 - membershipFunction.apply(x);
            return new FuzzySet(universe, complementFunc);
        } else {
            Map<Double, Double> newMemberships = new HashMap<>();
            for (double x : universe.getDiscretePoints()) {
                newMemberships.put(x, 1.0 - getMembership(x));
            }
            return new FuzzySet(universe, membershipFunction, newMemberships);
        }
    }

    public FuzzySet union(FuzzySet other) {
        if (universe.isDense()) {
            MembershipFunction unionFunc = x -> Math.max(
                    this.membershipFunction.apply(x),
                    other.membershipFunction.apply(x)
            );
            return new FuzzySet(universe, unionFunc);
        } else {
            Map<Double, Double> newMemberships = new HashMap<>();
            Set<Double> allKeys = new HashSet<>(this.membershipCache.keySet());
            allKeys.addAll(other.membershipCache.keySet());

            for (double x : allKeys) {
                double membership = Math.max(this.getMembership(x), other.getMembership(x));
                if (membership > 0.0) {
                    newMemberships.put(x, membership);
                }
            }

            return new FuzzySet(universe, membershipFunction, newMemberships);
        }
    }

    public FuzzySet intersection(FuzzySet other) {
        if (universe.isDense()) {
            MembershipFunction intersectionFunc = x -> Math.min(
                    this.membershipFunction.apply(x),
                    other.membershipFunction.apply(x)
            );
            return new FuzzySet(universe, intersectionFunc);
        } else {
            Map<Double, Double> newMemberships = new HashMap<>();

            FuzzySet smallerSet = this.membershipCache.size() < other.membershipCache.size() ? this : other;
            FuzzySet largerSet = this.membershipCache.size() < other.membershipCache.size() ? other : this;

            for (double x : smallerSet.membershipCache.keySet()) {
                double membership = Math.min(smallerSet.getMembership(x), largerSet.getMembership(x));
                if (membership > 0.0) {
                    newMemberships.put(x, membership);
                }
            }

            return new FuzzySet(universe, membershipFunction, newMemberships);
        }
    }

    public FuzzySet alphaCut(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Alpha must be in [0, 1]");
        }

        Set<Double> alphaMemberships = membershipCache.entrySet().stream()
                .filter(entry -> entry.getValue() >= alpha)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        double minVal = universe.getStart();
        double maxVal = universe.getEnd();
        return FuzzySet.classicSet(universe, minVal, maxVal, alphaMemberships);
    }

    public FuzzySet support() {
        if (universe.isDense()) {
            double start = universe.getStart();
            double end = universe.getEnd();
            int samples = 10000;
            double step = (end - start) / samples;

            double minSupport = end;
            double maxSupport = start;
            boolean foundSupport = false;

            for (double x = start; x <= end; x += step) {
                if (membershipFunction.apply(x) > 0) {
                    foundSupport = true;
                    if (x < minSupport) minSupport = x;
                    if (x > maxSupport) maxSupport = x;
                }
            }

            if (!foundSupport) {
                return FuzzySet.classicSet(universe, start, end, new HashSet<>());
            }

            FuzzySet crispSupport = new FuzzySet(universe, MembershipFunctions.crisp(minSupport, maxSupport));
            crispSupport.setClassic(true);
            return crispSupport;
        } else {
            Set<Double> supportElements = membershipCache.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0.0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            return FuzzySet.classicSet(universe, universe.getStart(), universe.getEnd(), supportElements);
        }
    }

    public FuzzySet core() {
        return alphaCut(1.0);
    }

    public double cardinalNumber() {
        if (universe.isDense()) {
            return computeClm();
        } else {
            if (isClassic) {
                return membershipCache.values().stream()
                        .filter(m -> m == 1.0)
                        .count();
            } else {
                return membershipCache.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();
            }
        }
    }

    private double computeClm() {
        if (!universe.isDense()) {
            throw new IllegalStateException("clm is only for continuous universes");
        }

        UnivariateFunction integrand = x -> membershipFunction.apply(x);
        SimpsonIntegrator integrator = new SimpsonIntegrator(1e-6, 1e-10, 2, 64);

        try {
            return integrator.integrate(10000, integrand, universe.getStart(), universe.getEnd());
        } catch (Exception e) {
            return trapezoidalIntegration(integrand, universe.getStart(), universe.getEnd(), 1000);
        }
    }

    private double trapezoidalIntegration(UnivariateFunction f, double a, double b, int n) {
        double h = (b - a) / n;
        double sum = 0.5 * (f.value(a) + f.value(b));

        for (int i = 1; i < n; i++) {
            double x = a + i * h;
            sum += f.value(x);
        }

        return sum * h;
    }

    public double supportMeasure() {
        if (universe.isDense()) {
            double start = universe.getStart();
            double end = universe.getEnd();
            int samples = 10000;
            double step = (end - start) / samples;

            double minSupport = end;
            double maxSupport = start;
            boolean foundSupport = false;

            for (double x = start; x <= end; x += step) {
                if (membershipFunction.apply(x) > 0) {
                    foundSupport = true;
                    if (x < minSupport) minSupport = x;
                    if (x > maxSupport) maxSupport = x;
                }
            }

            return foundSupport ? (maxSupport - minSupport) : 0.0;
        } else {
            return membershipCache.values().stream()
                    .filter(m -> m > 0.0)
                    .count();
        }
    }

    public double centroid() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot compute centroid of empty set");
        }

        double numerator = membershipCache.entrySet().stream()
                .mapToDouble(entry -> entry.getKey() * entry.getValue())
                .sum();

        double denominator = membershipCache.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return numerator / denominator;
    }

    public double degreeOfFuzziness() {
        double supportMeasureValue = supportMeasure();
        double universeMeasure = universe.getMeasure();
        return supportMeasureValue / universeMeasure;
    }

    public static FuzzySet classicSet(Universe universe, double start, double end) {
        FuzzySet returnSet = new FuzzySet(universe, MembershipFunctions.crisp(start, end));
        returnSet.setClassic(true);
        return returnSet;
    }

    public static FuzzySet classicSet(Universe universe, double start, double end, Set<Double> elements) {
        Map<Double, Double> memberships = new HashMap<>();
        for (double x : elements) {
            memberships.put(x, 1.0);
        }
        FuzzySet returnSet = new FuzzySet(universe, MembershipFunctions.crisp(start, end), memberships);
        returnSet.setClassic(true);
        return returnSet;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FuzzySet other)) return false;
        return universe.equals(other.universe) && membershipCache.equals(other.membershipCache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(universe, membershipCache);
    }

    @Override
    public String toString() {
        return "FuzzySet{universe=" + universe + ", cached=" + membershipCache.size() + "}";
    }

    public boolean isClassic() {
        return isClassic;
    }

    public void setClassic(boolean classic) {
        isClassic = classic;
    }
}