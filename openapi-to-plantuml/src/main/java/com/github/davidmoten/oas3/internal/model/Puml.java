package com.github.davidmoten.oas3.internal.model;

public final class Puml implements HasPuml {

    private final String puml;

    public Puml(String puml) {
        this.puml = puml;
    }

    @Override
    public String puml() {
        return puml;
    }

}
