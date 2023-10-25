package com.github.davidmoten.oas3.internal.model;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Lists;

public final class Model {

    public static final Model EMPTY = new Model(Collections.emptyList(), Collections.emptyList());

    private final List<Class> classes;
    private final List<? extends Relationship> relationships;

    private final Map<String, Class> map;

    public Model(List<Class> classes, List<? extends Relationship> relationships) {
        this.classes = classes;
        this.relationships = relationships;
        this.map = classes.stream().collect(Collectors.toMap(c -> c.name(), c -> c, (a, b) -> a));
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

    public List<? extends Relationship> relationships() {
        return relationships;
    }

    public Optional<Class> cls(String name) {
        return Optional.ofNullable(map.get(name));
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Model [");
        b.append(classes.stream().map(x -> "\n  " + x).collect(joining(",")));
        b.append(relationships.stream().map(x -> "\n  " + x).collect(joining(",")));
        b.append("\n]");
        return b.toString();
    }

}
