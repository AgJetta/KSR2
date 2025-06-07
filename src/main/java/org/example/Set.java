package org.example;

import java.util.List;

public abstract class Set {

    private UniverseOfDiscourse universeOfDiscourse;

    private List<element> support;

    public Set(UniverseOfDiscourse universeOfDiscourse, List<element> support) {
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

    public List<element> getSupport() {
        return support;
    }

}
