package com.github.davidmoten.oa2puml.v3;

import java.util.Collections;
import java.util.Map;

public class Util {
    
    public static <T, S> Map<T, S> nullToEmpty(Map<T, S> map) {
        if (map == null) {
            return Collections.emptyMap();
        } else {
            return map;
        }
    }

}
