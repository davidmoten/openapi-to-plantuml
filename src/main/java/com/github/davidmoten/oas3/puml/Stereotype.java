package com.github.davidmoten.oas3.puml;

enum Stereotype {
    PARAMETER("<<Parameter>>"), REQUEST_BODY("<<Request Body>>"), RESPONSE("<<Response>>");

    private final String name;

    private Stereotype(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}