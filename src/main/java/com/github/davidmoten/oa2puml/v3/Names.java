package com.github.davidmoten.oa2puml.v3;

import static com.github.davidmoten.oa2puml.v3.Util.nullToEmpty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.davidmoten.guavamini.Preconditions;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

public final class Names {

    private final Map<String, String> refClassNames = new HashMap<>();
    private final Set<String> classNames = new HashSet<>();

    public Names(OpenAPI a) {
        Components components = a.getComponents();
        if (components != null) {
            // resolve name clashes
            nullToEmpty(components.getSchemas()).keySet().forEach(name -> {
                String className = nextClassName(classNames, name);
                refClassNames.put("#/components/schemas/" + name, className);
            });
            nullToEmpty(components.getRequestBodies()).keySet().forEach(name -> {
                String className = nextClassName(classNames, name);
                refClassNames.put("#/components/requestBodies/" + name, className);
            });
            nullToEmpty(components.getParameters()).keySet().forEach(name -> {
                String className = nextClassName(classNames, name);
                refClassNames.put("#/components/parameters/" + name, className);
            });
            nullToEmpty(components.getResponses()).keySet().forEach(name -> {
                String className = nextClassName(classNames, name);
                refClassNames.put("#/components/responses/" + name, className);
            });
        }
    }

    public String refToClassName(String ref) {
        Preconditions.checkNotNull(ref);
        String className = refClassNames.get(ref);
        if (className == null) {
            throw new RuntimeException("could not find ref=" + ref);
        } else {
            return className;
        }
    }

    public String nextClassName(String candidate) {
        return nextClassName(classNames, candidate);
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
