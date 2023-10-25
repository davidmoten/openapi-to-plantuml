package com.github.davidmoten.oas3.internal.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.davidmoten.kool.Stream;

public final class ModelConverterExtract implements ModelConverter {

    private final Set<String> classNamesFrom;
    private boolean regex;

    public ModelConverterExtract(Set<String> classNamesFrom, boolean regex) {
        this.classNamesFrom = classNamesFrom;
        this.regex = regex;
    }

    @Override
    public Model apply(Model m) {
        Set<Class> set = m.classes().stream().filter(c -> {
            if (regex) {
                return classNamesFrom.stream().anyMatch(className -> {
                    Pattern pattern = Pattern.compile(className);
                    return pattern.matcher(c.name()).matches();
                });
            } else {
                return classNamesFrom.contains(c.name());
            }
        }).collect(Collectors.toSet());
        while (true) {
            int size = set.size();
            m.relationships().forEach(r -> {
                if (r instanceof Association) {
                    Association a = (Association) r;
                    Class c = m.cls(a.from()).get();
                    if (set.contains(c)) {
                        set.add(m.cls(a.to()).get());
                    }
                } else {
                    Inheritance a = (Inheritance) r;
                    List<Class> list = Stream.of(a.from()).concatWith(Stream.from(a.to())).map(x -> m.cls(x).get())
                            .toList().get();
                    if (list.stream().anyMatch(c -> set.contains(c))) {
                        set.addAll(list);
                    }
                }
            });
            if (set.size() == size) {
                break;
            }
        }
        set.forEach(c -> System.out.println("  " + c.name()));

        Map<String, Set<Association>> froms = new HashMap<>();
        associations(m) //
                .forEach(a -> {
                    Set<Association> s = froms.get(a.from());
                    if (s == null) {
                        s = new HashSet<>();
                        froms.put(a.from(), s);
                    }
                    s.add(a);
                });

        List<Class> classes = Stream.from(set) //
                .map(c -> {
                    Set<Association> ass = froms.getOrDefault(c.name(), Collections.emptySet());
                    List<Field> extras = Stream.from(ass) //
                            .filter(a -> !set.contains(m.cls(a.from()).get()))
                            .map(a -> new Field(a.propertyOrParameterName().orElse(a.to()), //
                                    a.to(), //
                                    a.type() == AssociationType.MANY, //
                                    a.type() == AssociationType.ZERO_ONE)) //
                            .toList() //
                            .get();
                    // note that all inheritance related classes will be present
                    if (extras.isEmpty()) {
                        return c;
                    } else {
                        List<Field> fields = Stream.from(c.fields()).concatWith(Stream.from(extras)).toList().get();
                        return new Class(c.name(), c.type(), fields, c.isEnum());
                    }
                }) //
                .toList().get();

        List<Relationship> rels = m.relationships() //
                .stream() //
                .filter(r -> {
                    if (r instanceof Association) {
                        Association a = (Association) r;
                        return set.contains(m.cls(a.from()).get()) && set.contains(m.cls(a.to()).get());
                    } else {
                        Inheritance a = (Inheritance) r;
                        return Stream.of(a.from()).concatWith(Stream.from(a.to())).map(x -> m.cls(x).get())
                                .any(x -> classes.contains(x)).get();
                    }
                }) //
                .collect(Collectors.toList());
        return new Model(classes, rels);
    }

    private static Stream<Association> associations(Model m) {
        return Stream.from(m.relationships()).filter(r -> r instanceof Association) //
                .map(r -> (Association) r);
    }

}
