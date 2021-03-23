package com.github.davidmoten.oa2puml.v3;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.davidmoten.guavamini.Preconditions;

import io.swagger.v3.oas.models.OpenAPI;

public final class Names {

    private final Map<String, String> refClassNames = new HashMap<>();
    private final Set<String> classNames = new HashSet<>();

    public Names(OpenAPI a) {
        if (a.getComponents() != null) {
            nullToEmpty(a.getComponents().getSchemas()).keySet().forEach(name -> {
                String className = nextClassName(classNames, name);
                refClassNames.put("#/components/schemas/" + name, className);
            });
            nullToEmpty(a.getComponents().getRequestBodies()).keySet().forEach(name -> {
                String className = nextClassName(classNames, name);
                refClassNames.put("#/components/requestBodies/" + name, className);
            });
        }
    }

    private static <T, S> Map<T, S> nullToEmpty(Map<T, S> map) {
        if (map == null) {
            return Collections.emptyMap();
        } else {
            return map;
        }
    }

    private static String nextClassName(Set<String> classNames, String... candidates) {
        return nextClassName(classNames, Arrays.asList(candidates));
    }

    private static String nextClassName(Set<String> classNames, List<String> candidates) {
        Preconditions.checkArgument(!candidates.isEmpty());
        for (String candidate : candidates) {
            if (!classNames.contains(candidate)) {
                classNames.add(candidate);
                return candidate;
            }
        }
        int i = 1;
        String lastCandidate = candidates.get(candidates.size() - 1);
        while (true) {
            String className = lastCandidate + "." + i;
            if (!classNames.contains(className)) {
                classNames.add(className);
                return className;
            }
            i++;
        }
    }
}
