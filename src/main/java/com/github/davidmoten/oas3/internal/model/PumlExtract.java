package com.github.davidmoten.oas3.internal.model;

import java.util.Set;

public final class PumlExtract implements HasPuml{

    private final String puml;
    private final Set<String> classNameFrom;

    public PumlExtract(String puml, Set<String> classNameFrom) {
        this.puml = puml;
        this.classNameFrom = classNameFrom;
    }
    
    @Override
    public String puml() {
        return puml;
    }
    
    public Set<String> classNameFrom() {
        return classNameFrom;
    }

}
