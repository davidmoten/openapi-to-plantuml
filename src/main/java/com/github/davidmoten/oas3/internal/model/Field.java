package com.github.davidmoten.oas3.internal.model;

public final class Field {

    private final String name;
    // TODO convert to simpleType
    private final String type;
    private final boolean isArray;
    private final boolean required;
    private int maxLength;
    private String description;
    private String example;
    private String format;
    private String extension;


    public Field(String name, String type, boolean isArray, boolean required) {
        this.name = name;
        this.type = type;
        this.isArray = isArray;
        this.required = required;
    }

    public Field(String name, String type, boolean isArray, boolean required, int maxLength, String description, String example, String format, String extension) {
        this.name = name;
        this.type = type;
        this.isArray = isArray;
        this.required = required;
        this.maxLength = maxLength;
        this.description = description;
        this.example = example;
        this.format = format;
        this.extension = extension;
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

    public int maxLength() {
        return maxLength;
    }

    public String description() {
        return description;
    }

    public String example() {
        return example;
    }

    public String format() {
        return format;
    }

    public String extension() {
        return extension;
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
