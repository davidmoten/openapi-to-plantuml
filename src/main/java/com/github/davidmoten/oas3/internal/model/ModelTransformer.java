package com.github.davidmoten.oas3.internal.model;

import java.util.function.Function;

public interface ModelTransformer<T extends HasUml> extends Function<Model, Model> {

    T createHasUml(String puml);

    static <T extends HasUml> ModelTransformer<T> identity() {
        return new ModelTransformer<T>() {

            @Override
            public Model apply(Model t) {
                return t;
            }

            @SuppressWarnings("unchecked")
            @Override
            public T createHasUml(String uml) {
                return (T) new Uml(uml);
            }
        };
    }

}
