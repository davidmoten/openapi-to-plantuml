package com.github.davidmoten.oas3.internal;

import static com.github.davidmoten.oas3.internal.Util.nullMapToEmpty;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

public final class Names {

    private static final String EMPTY_RESPONSE_CLASS_NAME = "Empty Response";
    public static final String NAMESPACE_DELIMITER = "::";
    private final Map<String, Reference> refClassNames = new HashMap<>();
    private final Set<String> classNames = Sets.newHashSet(EMPTY_RESPONSE_CLASS_NAME);
    private final OpenAPI openapi;

    public Names(OpenAPI a) {
        Components components = a.getComponents();
        this.openapi = a;
        if (components != null) {
            // resolve name clashes
            nullMapToEmpty(components.getSchemas()).keySet().forEach(name -> 
                refClassNames.put("#/components/schemas/" + name, new Reference(name, classNames))
            );
            nullMapToEmpty(components.getRequestBodies()).keySet().forEach(name -> 
                refClassNames.put("#/components/requestBodies/" + name, new Reference(name, classNames))
            );
            nullMapToEmpty(components.getParameters()).keySet().forEach(name -> 
                refClassNames.put("#/components/parameters/" + name, new Reference(name, classNames))
            );
            nullMapToEmpty(components.getResponses()).keySet().forEach(name -> 
                refClassNames.put("#/components/responses/" + name, new Reference(name, classNames))
            );
        }
    }

    String schemaClassName(String simpleName) {
        return refToClassName("#/components/schemas/" + simpleName).className();
    }

    String requestBodyClassName(String simpleName) {
        return refToClassName("#/components/requestBodies/" + simpleName).className();
    }

    String responseClassName(String simpleName) {
        return refToClassName("#/components/responses/" + simpleName).className();
    }

    private String parameterClassName(String simpleName) {
        return refToClassName("#/components/parameters/" + simpleName).className();
    }

    String requestBodyClassName(RequestBody b) {
        return nullMapToEmpty(components().getRequestBodies()) //
                .entrySet() //
                .stream() //
                .filter(entry -> entry.getValue() == b) //
                .map(entry -> requestBodyClassName(entry.getKey())) //
                .findFirst() //
                .orElseThrow(() -> new RuntimeException("cound not find " + b));
    }

    String parameterClassName(Parameter p) {
        return nullMapToEmpty(components().getParameters()) //
                .entrySet() //
                .stream() //
                .filter(entry -> entry.getValue() == p) //
                .map(entry -> parameterClassName(entry.getKey())) //
                .findFirst() //
                .orElseThrow(() -> new RuntimeException("cound not find " + p));
    }

    Reference refToClassName(String ref) {
        Preconditions.checkNotNull(ref);
        Reference reference = refClassNames.get(ref);
        if (reference == null) {
            Reference r = new Reference(ref, classNames);
            refClassNames.put(ref, r);
            // throw new RuntimeException("could not find ref=" + ref);
            return r;
        } else {
            return reference;
        }
    }

    public static final class Reference {
        final Optional<String> base;
        final String name;
        final String className;

        Reference(String ref, Set<String> classNames) {
            int i = ref.lastIndexOf("#");
            if (i == -1) {
                base = Optional.empty();
                name = ref;
            } else {
                if (i == 0) {
                    base = Optional.empty();
                } else {
                    String s = ref.substring(0, i);
                    if (s.startsWith("./")) {
                        s = s.substring(2);
                    }
                    base = Optional.of(s);
                }
                String nm = ref.substring(i + 1);
                int j = nm.substring(0, nm.length() - 1).lastIndexOf("/");
                if (j == -1) {
                    name = nm;
                } else {
                    name = nm.substring(j + 1);
                }
            }
            className = nextClassName(classNames, name);
        }

        public String className() {
            return base.map(x -> x + NAMESPACE_DELIMITER).orElse("") + className;
        }
    }

    Components components() {
        return openapi.getComponents();
    }

    Paths paths() {
        return openapi.getPaths();
    }

    String nextClassName(String candidate) {
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

    @SuppressWarnings("rawtypes")
    Map<String, Schema> schemas() {
        if (openapi.getComponents() == null) {
            return Collections.emptyMap();
        } else {
            return nullMapToEmpty(openapi.getComponents().getSchemas());
        }
    }

    Map<String, RequestBody> requestBodies() {
        if (openapi.getComponents() == null) {
            return Collections.emptyMap();
        } else {
            return nullMapToEmpty(openapi.getComponents().getRequestBodies());
        }
    }

    Map<String, Parameter> parameters() {
        if (openapi.getComponents() == null) {
            return Collections.emptyMap();
        } else {
            return nullMapToEmpty(openapi.getComponents().getParameters());
        }
    }

    Map<String, ApiResponse> responses() {
        if (openapi.getComponents() == null) {
            return Collections.emptyMap();
        } else {
            return nullMapToEmpty(openapi.getComponents().getResponses());
        }
    }
}
