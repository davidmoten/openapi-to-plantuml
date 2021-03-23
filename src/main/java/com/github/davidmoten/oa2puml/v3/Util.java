package com.github.davidmoten.oa2puml.v3;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public final class Util {

    private Util() {
        // prevent instantiation
    }

    public static <T, S> Map<T, S> nullToEmpty(Map<T, S> map) {
        if (map == null) {
            return Collections.emptyMap();
        } else {
            return map;
        }
    }

    public static <T, S> Optional<Entry<T, S>> first(Map<T, S> map) {
        return map.entrySet().stream().findFirst();
    }

}
