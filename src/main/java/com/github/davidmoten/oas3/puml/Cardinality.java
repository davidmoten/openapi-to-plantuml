package com.github.davidmoten.oas3.puml;

enum Cardinality {
    ZERO_ONE("0..1"), ONE("1"), MANY("*"), ALL("all");
    private final String string;

    private Cardinality(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}