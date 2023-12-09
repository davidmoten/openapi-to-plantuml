package com.github.davidmoten.oas3.uml;

import java.util.Set;

import com.github.davidmoten.oas3.internal.model.HasUml;

public final class UmlExtract implements HasUml {

    private final String puml;
    private final Set<String> classNameFrom;

    public UmlExtract(String uml, Set<String> classNameFrom) {
        this.puml = uml;
        this.classNameFrom = classNameFrom;
    }

    @Override
    public String uml() {
        return puml;
    }

    public Set<String> classNameFrom() {
        return classNameFrom;
    }

}
