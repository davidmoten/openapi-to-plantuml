package com.github.davidmoten.oas3.internal.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;

public final class Class {
    private final String name;
    private final ClassType type;
    private final List<Field> fields;
    private final boolean isEnum;

    public Class(String name, ClassType type, List<Field> fields, boolean isEnum) {
        this.isEnum = isEnum;
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(fields);
        this.name = name;
        this.type = type;
        this.fields = fields;
    }

    public Class(String name, ClassType type) {
        this(name, type, Collections.emptyList(), false);
    }

    public String name() {
        return name;
    }

    public ClassType type() {
        return type;
    }

    public List<Field> fields() {
        return fields;
    }

    public boolean isEnum() {
        return isEnum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, isEnum, name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Class other = (Class) obj;
        return Objects.equals(fields, other.fields) && isEnum == other.isEnum && Objects.equals(name, other.name)
                && type == other.type;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Class [name=");
        b.append(name);
        b.append(", type=");
        b.append(type);
        b.append(", fields=");
        b.append(fields);
        b.append("]");
        return b.toString();
    }

}
