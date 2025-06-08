package org.example;

import org.example.membershipFunctions.MembershipFunctions;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        FuzzySetExample.main(args);
    }
}

class FuzzySetExample {
    public static void main(String[] args) {
        // Create universe
        Universe universe = new Universe(0.0, 10.0, false, 0.5);

        // Create triangular fuzzy set
        FuzzySet triangular = new FuzzySet(universe,
                MembershipFunctions.triangular(2.0, 5.0, 8.0));

        // Create classic set
        FuzzySet classic = FuzzySet.classicSet(universe, 3.0, 7.0);

        // Operations
        FuzzySet union = triangular.union(classic);
        FuzzySet intersection = triangular.intersection(classic);
        FuzzySet complement = triangular.complement();

        // Properties
        System.out.println("Triangular set height: " + triangular.height());
        System.out.println("Is normal: " + triangular.isNormal());
        System.out.println("Is convex: " + triangular.isConvex());
        System.out.println("Cardinality: " + triangular.cardinality());
        System.out.println("Centroid: " + triangular.centroid());
    }
}