package com.github.davidmoten.oas3.internal.model;

public class Field {

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

    public void setMaxLength(int length) {
        this.maxLength = length;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String example() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String format() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String extension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
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
