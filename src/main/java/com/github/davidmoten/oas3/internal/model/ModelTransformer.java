package com.github.davidmoten.oas3.internal.model;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface ModelTransformer extends Function<Model, List<Model>> {

}
