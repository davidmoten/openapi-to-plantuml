package com.github.davidmoten.oas3.internal.model;

public final class Uml implements HasUml {

    private final String puml;

    public Uml(String puml) {
        this.puml = puml;
    }

    @Override
    public String uml() {
        return puml;
    }

}
