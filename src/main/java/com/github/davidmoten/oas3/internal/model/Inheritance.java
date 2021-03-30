package com.github.davidmoten.oas3.internal.model;

import java.util.List;
import java.util.Optional;

public class Inheritance implements Relationship{
    private final String from;
    private final List<String> to;
    private final AssociationType type;
    private final Optional<String> label;
    
    public Inheritance(String from, List<String> to, AssociationType type, Optional<String> label) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.label = label;
    }
    
    public String from() {
        return from;
    }

    public List<String> to() {
        return to;
    }

    public AssociationType type() {
        return type;
    }

    public Optional<String> label() {
        return label;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Inheritance [from=");
        b.append(from);
        b.append(", to=");
        b.append(to);
        b.append(", type=");
        b.append(type);
        b.append(", label=");
        b.append(label);
        b.append("]");
        return b.toString();
    }
    
    
}
