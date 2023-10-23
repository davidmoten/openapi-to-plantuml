package com.github.davidmoten.oas3.internal.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.davidmoten.kool.Stream;

public class ModelConverterExtract implements ModelConverter {

    private final Pattern classNamePatternFrom;

    public ModelConverterExtract(String classNamePatternFrom) {
        this.classNamePatternFrom = Pattern.compile(classNamePatternFrom);
    }

    @Override
    public Model apply(Model m) {
        Set<Class> set = m.classes().stream().filter(x -> classNamePatternFrom.matcher(x.name()).matches())
                .collect(Collectors.toSet());
        Set<Relationship> rels = new HashSet<>();
        while (true) {
            int size = set.size();
            m.relationships().forEach(r -> {
                if (r instanceof Association) {
                    Association a = (Association) r;
                    Class c = m.cls(a.from()).get();
                    if (set.contains(c)) {
                        set.add(m.cls(a.to()).get());
                        rels.add(r);
                    }
                } else {
                    Inheritance a = (Inheritance) r;
                    List<Class> list = Stream.of(a.from()).concatWith(Stream.from(a.to())).map(x -> m.cls(x).get()).toList().get();
                    if (list.stream().anyMatch(c -> set.contains(c))) {
                        set.addAll(list);
                        rels.add(r);
                    }
                }
            });
            if (set.size() == size) {
                break;
            }
        }
        
        List<Class> classes = Stream.from(m.classes()) //
                .map(c -> {
                    List<Field> extras = Stream.from(set) //
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
                        return new Class(c.name(), c.type(), fields, c.isEnum());
                    }
                }) //
                .toList().get();
        
        
        return m;
    }

}
