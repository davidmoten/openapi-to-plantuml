package com.github.davidmoten.oas3.model;

import java.util.Collections;
import java.util.List;

import com.github.davidmoten.guavamini.Lists;

public class Model {

    public static final Model EMPTY = new Model(Collections.emptyList(), Collections.emptyList());
    
    private final List<Class> classes;
    private final List<Relationship> relationships;

    public Model(List<Class> classes, List<Relationship> relationships) {
        this.classes = classes;
        this.relationships = relationships;
    }
    
    public Model(Class cls, Relationship r) {
        this(Collections.singletonList(cls), Collections.singletonList(r));
    }

    public Model add(Model model) {
        List<Class> c = Lists.newArrayList(classes);
        List<Relationship> r = Lists.newArrayList(relationships);
        c.addAll(model.classes);
        r.addAll(model.relationships);
        return new Model(c, r);
    }
    
    public List<Class> classes() {
        return classes;
    }
    
    public List<Relationship>  relationships() {
        return relationships;
    }

}
