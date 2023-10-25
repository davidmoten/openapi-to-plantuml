package com.github.davidmoten.oas3.internal.model;

import java.util.function.Function;

public interface ModelTransformer<T extends HasPuml> extends Function<Model, Model> {

    T createHasPuml(String puml);

    static <T extends HasPuml> ModelTransformer<T> identity() {
        return new ModelTransformer<T>() {

            @Override
            public Model apply(Model t) {
                return t;
            }

            @SuppressWarnings("unchecked")
            @Override
            public T createHasPuml(String puml) {
                return (T) new Puml(puml);
            }
        };
    }

}
