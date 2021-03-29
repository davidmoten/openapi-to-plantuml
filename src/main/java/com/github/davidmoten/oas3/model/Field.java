package com.github.davidmoten.oas3.model;

public final class Field {

    private final String name;
    //TODO convert to simpleType
    private final String type;
    private final boolean isArray;
    private final boolean required;

    public Field(String name, String type, boolean isArray, boolean required) {
        this.name = name;
        this.type = type;
        this.isArray = isArray;
        this.required = required;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Field [name=");
        b.append(name);
        b.append(", type=");
        b.append(type);
        b.append(", isArray=");
        b.append(isArray);
        b.append(", required=");
        b.append(required);
        b.append("]");
        return b.toString();
    }
    
    
    
}
