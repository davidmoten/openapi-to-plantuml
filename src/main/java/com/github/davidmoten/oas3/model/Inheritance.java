package com.github.davidmoten.oas3.model;

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
}
