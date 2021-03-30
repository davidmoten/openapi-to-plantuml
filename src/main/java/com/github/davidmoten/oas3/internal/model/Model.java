package com.github.davidmoten.oas3.internal.model;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
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

    public Model(Class cls) {
        this(Collections.singletonList(cls), Collections.emptyList());
    }

    public Model(Relationship r) {
        this(Collections.emptyList(), Collections.singletonList(r));
    }

    public Model add(Model model) {
        List<Class> c = Lists.newArrayList(classes);
        List<Relationship> r = Lists.newArrayList(relationships);
        c.addAll(model.classes);
        r.addAll(model.relationships);
        return new Model(c, r);
    }

    public Model add(Relationship r) {
        List<Relationship> list = new ArrayList<>(relationships);
        list.add(r);
        return new Model(classes, list);
    }

    public List<Class> classes() {
        return classes;
    }

    public List<Relationship> relationships() {
        return relationships;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Model [classes=");
        b.append(classes.stream().map(x -> "\n" + x).collect(joining()));
        b.append(", relationships=");
        b.append(relationships.stream().map(x -> "\n" + x).collect(joining()));
        b.append("]");
        return b.toString();
    }

}
