package com.github.davidmoten.oas3.puml2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public final class Util {

    private Util() {
        // prevent instantiation
    }

    public static <T, S> Map<T, S> nullMapToEmpty(Map<T, S> map) {
        if (map == null) {
            return Collections.emptyMap();
        } else {
            return map;
        }
    }
    
    public static <T> List<T> nullListToEmpty(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list;
        }
    }


    public static <T, S> Optional<Entry<T, S>> first(Map<T, S> map) {
        return map.entrySet().stream().findFirst();
    }
    
    public static String quote(String s) {
        return "\"" + s + "\"";
    }

}
