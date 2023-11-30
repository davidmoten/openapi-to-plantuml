package com.github.davidmoten.oas3.internal.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.davidmoten.kool.Stream;

public final class ModelTransformerLinksThreshold implements ModelTransformer<Uml> {

    private final int threshold;

    public ModelTransformerLinksThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public Model apply(Model m) {
        Map<String, Integer> counts = new HashMap<>();
        associations(m).forEach(a -> {
            addToCounts(a.from(), counts);
            addToCounts(a.to(), counts);
        });

        Set<String> classesToTrim = Stream.from(counts.entrySet()) //
                .filter(entry -> entry.getValue() > threshold) //
                .map(entry -> entry.getKey()) //
                .toSet() //
                .get();
        classesToTrim.forEach(System.out::println);

        List<? extends Relationship> rels = associations(m) //
                .filter(a -> !classesToTrim.contains(a.from()) && !classesToTrim.contains(a.to())) //
                .toList() //
                .get();

        Map<String, Set<Association>> froms = new HashMap<>();
        associations(m) //
                .forEach(a -> {
                    Set<Association> set = froms.get(a.from());
                    if (set == null) {
                        set = new HashSet<>();
                        froms.put(a.from(), set);
                    }
                    set.add(a);
                });

        List<Class> classes = Stream.from(m.classes()) //
                .map(c -> {
                    Set<Association> set = froms.getOrDefault(c.name(), Collections.emptySet());
                    List<Field> extras = Stream.from(set) //
                            .filter(a -> classesToTrim.contains(a.to())) //
                            .map(a -> new Field(a.propertyOrParameterName().orElse(a.to()), //
                                    a.to(), //
                                    a.type() == AssociationType.MANY, //
                                    a.type() == AssociationType.ZERO_ONE)) //
                            .toList() //
                            .get();
                    if (extras.isEmpty()) {
                        return c;
                    } else {
                        List<Field> fields = Stream.from(c.fields()).concatWith(Stream.from(extras)).toList().get();
                        return new Class(c.name(), c.type(), fields, c.isEnum(), c.description());
                    }
                }) //
                .toList().get();
        return new Model(classes, rels);
    }

    private static Stream<Association> associations(Model m) {
        return Stream.from(m.relationships()).filter(r -> r instanceof Association) //
                .map(r -> (Association) r);
    }

    private static void addToCounts(String className, Map<String, Integer> map) {
        Integer count = map.getOrDefault(className, 0);
        map.put(className, count + 1);
    }

    @Override
    public Uml createHasUml(String puml) {
        return new Uml(puml);
    }

}
