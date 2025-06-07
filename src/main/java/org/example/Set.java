package org.example;

import java.util.List;

public abstract class Set {

    private UniverseOfDiscourse universeOfDiscourse;

    private List<Element> support;

    public Set(UniverseOfDiscourse universeOfDiscourse, List<Element> support) {
        this.universeOfDiscourse = universeOfDiscourse;
        this.support = support;
    }

    public Set union (Set other) {
        //functionalities need to be added
        return other;
    }


    public Set intersection (Set other) {
        //functionalities need to be added
        return other;
    }

    public List<Element> getSupport() {
        return support;
    }

}
