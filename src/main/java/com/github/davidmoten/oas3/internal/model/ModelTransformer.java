package com.github.davidmoten.oas3.internal.model;

import java.util.function.Function;

public interface ModelTransformer<T extends HasPuml> extends Function<Model, Model> {
    
    T createHasPuml(String puml);

}
