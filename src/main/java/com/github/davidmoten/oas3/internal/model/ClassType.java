package com.github.davidmoten.oas3.internal.model;

public enum ClassType {

    SCHEMA("Schema"), PARAMETER("Parameter"), REQUEST_BODY("RequestBody"), RESPONSE("Response"),
    METHOD("Method");

    private final String name;

    private ClassType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
