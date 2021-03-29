package com.github.davidmoten.oas3.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Class {
    private final String name;
    private final ClassType type;
    private final List<Field> fields;

    public Class(String name, ClassType classType, 
            List<Field> fields) {
        this.name = name;
        this.type = classType;
        this.fields = fields;
    }
    
    public Class(String name, ClassType classType) {
        this(name, classType, Collections.emptyList());
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
    
}
