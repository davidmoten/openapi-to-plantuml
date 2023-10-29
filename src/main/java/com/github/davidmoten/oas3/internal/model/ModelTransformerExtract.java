package com.github.davidmoten.oas3.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.davidmoten.kool.Stream;

public final class ModelTransformerExtract implements ModelTransformer<PumlExtract> {

    private final Set<String> classNamesFrom;
    private boolean regex;

    public ModelTransformerExtract(Set<String> classNamesFrom, boolean regex) {
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

        Map<String, Set<Class>> subClasses = new HashMap<>();
        inheritances(m) //
                .forEach(inh -> {
                    for (String to : inh.to()) {
                        m.cls(to).ifPresent(c -> {
                            Set<Class> s = subClasses.get(inh.from());
                            if (s == null) {
                                s = new HashSet<>();
                                subClasses.put(inh.from(), s);
                            }
                            s.add(c);
                        });
                    }
                });

        Map<String, Set<Inheritance>> superClasses = new HashMap<>();
        inheritances(m) //
                .forEach(a -> a //
                        .to() //
                        .stream() //
                        .forEach(x -> {
                            m.cls(x).ifPresent(c -> {
                                Set<Inheritance> s = superClasses.get(x);
                                if (s == null) {
                                    s = new HashSet<>();
                                    superClasses.put(x, s);
                                }
                                s.add(a);
                            });
                        }));

        for (Class c : new ArrayList<>(set)) {
            addRelated(m, set, froms, superClasses, subClasses, c);
        }

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

        List<Relationship> rels = Stream //
                .from(m.relationships()) //
                .flatMap(r -> {
                    if (r instanceof Association) {
                        Association a = (Association) r;
                        if (set.contains(m.cls(a.from()).get()) //
                                && set.contains(m.cls(a.to()).get())) {
                            return Stream.of(r);
                        } else {
                            return Stream.empty();
                        }
                    } else {
                        Inheritance a = (Inheritance) r;
                        List<String> tos = Stream //
                                .from(a.to()) //
                                .map(x -> m.cls(x).get()) //
                                .filter(x -> classes.contains(x)) //
                                .map(c -> c.name()) //
                                .toList() //
                                .get();
                        if (tos.isEmpty()) {
                            return Stream.empty();
                        } else {
                            return Stream.of(new Inheritance(a.from(), tos, a.type(), a.propertyName()));
                        }
                    }
                }) //
                .toList().get();
        return new Model(classes, rels);
    }

    private static void addRelated(Model model, Set<Class> set, Map<String, Set<Association>> froms,
            Map<String, Set<Inheritance>> superClasses, Map<String, Set<Class>> subClasses, Class a) {
        set.add(a);
        Set<Association> associations = froms.getOrDefault(a.name(), Collections.emptySet());
        for (Association ass : associations) {
            model.cls(ass.to()).ifPresent(cls -> {
                if (!set.contains(cls)) {
                    addRelated(model, set, froms, superClasses, subClasses, cls);
                }
            });
        }
        Set<Class> subs = subClasses.getOrDefault(a.name(), Collections.emptySet());
        for (Class sub : subs) {
            if (!set.contains(sub)) {
                addRelated(model, set, froms, superClasses, subClasses, sub);
            }
        }
        Set<Inheritance> supers = superClasses.getOrDefault(a.name(), Collections.emptySet());
        for (Inheritance sup : supers) {
            model.cls(sup.from()).ifPresent(cls -> {
                if (!set.contains(cls)) {
                    addRelated(model, set, froms, superClasses, subClasses, cls);
                }
            });
            for (String to : sup.to()) {
                model.cls(to).ifPresent(cls -> {
                    if (!set.contains(cls)) {
                        addRelated(model, set, froms, superClasses, subClasses, cls);
                    }
                });
            }
        }
    }

    private static Stream<Association> associations(Model m) {
        return Stream //
                .from(m.relationships()) //
                .filter(r -> r instanceof Association) //
                .map(r -> (Association) r);
    }

    private static Stream<Inheritance> inheritances(Model m) {
        return Stream //
                .from(m.relationships()) //
                .filter(r -> r instanceof Inheritance) //
                .map(r -> (Inheritance) r);
    }

    @Override
    public PumlExtract createHasPuml(String puml) {
        return new PumlExtract(puml, classNamesFrom);
    }

}
