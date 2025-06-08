package org.fuzzy;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum LogicalConnective {
    AND(FuzzySet::intersection),
    OR(FuzzySet::union),
    NOT(FuzzySet::complement);

    private final BiFunction<FuzzySet, FuzzySet, FuzzySet> binaryOp;
    private final Function<FuzzySet, FuzzySet> unaryOp;

    LogicalConnective(BiFunction<FuzzySet, FuzzySet, FuzzySet> op) {
        this.binaryOp = op;
        this.unaryOp = null;
    }

    LogicalConnective(Function<FuzzySet, FuzzySet> op) {
        this.binaryOp = null;
        this.unaryOp = op;
    }

    public FuzzySet apply(FuzzySet a, FuzzySet b) {
        if (binaryOp == null)
            throw new UnsupportedOperationException("Unary connective used with two operands");
        return binaryOp.apply(a, b);
    }

    public FuzzySet apply(FuzzySet a) {
        if (unaryOp == null)
            throw new UnsupportedOperationException("Binary connective used with one operand");
        return unaryOp.apply(a);
    }
}
