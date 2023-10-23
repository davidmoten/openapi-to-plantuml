package com.github.davidmoten.oas3.internal.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Inheritance implements Relationship {
    private final String from;
    private final List<String> to;
    private final AssociationType type;
    private final Optional<String> propertyName;

    public Inheritance(String from, List<String> to, AssociationType type, Optional<String> propertyName) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.propertyName = propertyName;
    }

    public String from() {
        return from;
    }

    public List<String> to() {
        return to;
    }

    public AssociationType type() {
        return type;
    }

    public Optional<String> propertyName() {
        return propertyName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Inheritance [from=");
        b.append(from);
        b.append(", to=");
        b.append(to);
        b.append(", type=");
        b.append(type);
        b.append(", propertyName=");
        b.append(propertyName);
        b.append("]");
        return b.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, propertyName, to, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Inheritance other = (Inheritance) obj;
        return Objects.equals(from, other.from) && Objects.equals(propertyName, other.propertyName)
                && Objects.equals(to, other.to) && type == other.type;
    }
    
    

}
