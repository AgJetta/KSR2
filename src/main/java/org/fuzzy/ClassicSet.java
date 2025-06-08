package org.fuzzy;

import java.util.List;

public abstract class ClassicSet {

    private Universe universe;

    private List<Element> support;

    public ClassicSet(Universe universe, List<Element> support) {
        this.universe = universe;
        this.support = support;
    }

    public ClassicSet union (ClassicSet other) {
        //functionalities need to be added
        return other;
    }


    public ClassicSet intersection (ClassicSet other) {
        //functionalities need to be added
        return other;
    }

    public List<Element> getSupport() {
        return support;
    }

}
