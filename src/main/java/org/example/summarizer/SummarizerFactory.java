package org.example.summarizer;

import org.example.FuzzySet;
import org.example.Universe;
import org.example.membershipFunctions.MembershipFunction;
import org.example.membershipFunctions.MembershipFunctions;

// Factory class for creating common summarizers
public class SummarizerFactory {

    // High energy summarizer (assuming energy field 0-1)
    public static Summarizer highEnergy() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzySet = new FuzzySet(universe, MembershipFunctions.trapezoidal(0.6, 0.8, 1.0, 1.0));
        return new Summarizer("high energy", "energy", fuzzySet);
    }

    // Low energy summarizer
    public static Summarizer lowEnergy() {
        Universe universe = new Universe(0.0, 1.0, true);
        FuzzySet fuzzySet = new FuzzySet(universe, MembershipFunctions.trapezoidal(0.0, 0.0, 0.2, 0.4));
        return new Summarizer("low energy", "energy", fuzzySet);
    }

    // High tempo summarizer (assuming tempo 60-200 BPM)
    public static Summarizer fastTempo() {
        Universe universe = new Universe(60.0, 200.0, true);
        FuzzySet fuzzySet = new FuzzySet(universe, MembershipFunctions.trapezoidal(140.0, 160.0, 200.0, 200.0));
        return new Summarizer("fast tempo", "tempo", fuzzySet);
    }

    // Slow tempo summarizer
    public static Summarizer slowTempo() {
        Universe universe = new Universe(60.0, 200.0, true);
        FuzzySet fuzzySet = new FuzzySet(universe, MembershipFunctions.trapezoidal(60.0, 60.0, 80.0, 100.0));
        return new Summarizer("slow tempo", "tempo", fuzzySet);
    }

    // High popularity summarizer (assuming popularity 0-100)
    public static Summarizer popular() {
        Universe universe = new Universe(0.0, 100.0, true);
        FuzzySet fuzzySet = new FuzzySet(universe, MembershipFunctions.trapezoidal(70.0, 85.0, 100.0, 100.0));
        return new Summarizer("popular", "popularity", fuzzySet);
    }

    // Custom summarizer factory method
    public static Summarizer custom(String name, String fieldName, Universe universe, MembershipFunction membershipFunction) {
        FuzzySet fuzzySet = new FuzzySet(universe, membershipFunction);
        return new Summarizer(name, fieldName, fuzzySet);
    }
}
