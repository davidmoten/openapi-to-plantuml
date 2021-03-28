package com.github.davidmoten.oas3.puml;

import static com.github.davidmoten.oas3.puml.Util.first;
import static com.github.davidmoten.oas3.puml.Util.nullMapToEmpty;
import static com.github.davidmoten.oas3.puml.Util.quote;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

import java.util.Collections;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import static com.github.davidmoten.oas3.puml.Constants.*;

public final class ComponentsHelper {

    static String toPlantUml(Names names) {
        String part1 = names.schemas() //
                .entrySet() //
                .stream() //
                .map(entry -> Common.toPlantUmlClass(names.schemaClassName(entry.getKey()),
                        entry.getValue(), names)) //
                .collect(joining());

        String part2 = names.requestBodies() //
                .entrySet() //
                .stream() //
                .map(entry -> {
                    RequestBody b = entry.getValue();
                    String className = names.requestBodyClassName(b);
                    String ref = b.get$ref();
                    if (ref != null) {
                        String otherClassName = names.refToClassName(ref);
                        String classDefinition = Constants.NL + "class " + quote(className)
                                + Stereotype.REQUEST_BODY + "{}";
                        return classDefinition + NL + quote(className)
                                + CLASS_RELATIONSHIP_RIGHT_ARROW + SPACE + quote("1") + SPACE
                                + quote(otherClassName);
                    } else {
                        return Common.toPlantUmlClass(names.requestBodyClassName(entry.getKey()),
                                first(entry.getValue().getContent()).get().getValue().getSchema(),
                                names, singletonList(Stereotype.REQUEST_BODY.toString()),
                                Collections.emptyList());
                    }
                }) //
                .collect(joining());

        String part3 = names.parameters() //
                .entrySet() //
                .stream() //
                .map(entry -> {
                    Parameter p = entry.getValue();
                    String className = names.parameterClassName(p);
                    String ref = p.get$ref();
                    if (ref != null) {
                        String classDefinition = NL + NL + "class " + quote(className) + SPACE
                                + Stereotype.PARAMETER + "{}";
                        String otherClassName = names.refToClassName(ref);
                        String relationship = NL + NL + quote(className)
                                + CLASS_RELATIONSHIP_RIGHT_ARROW + SPACE + quote("1") + SPACE
                                + quote(otherClassName);
                        return classDefinition + relationship;
                    } else {
                        return Common.toPlantUmlClass(className, p.getSchema(), names,
                                Stereotype.PARAMETER);
                    }
                }) //
                .collect(joining());

        String part4 = names.responses() //
                .entrySet() //
                .stream() //
                // TODO handle ref responses as per parameters and request bodies above
                .map(entry -> first(nullMapToEmpty(entry.getValue().getContent())) //
                        .map(x -> Common.toPlantUmlClass(names.responseClassName(entry.getKey()),
                                x.getValue().getSchema(), names,
                                singletonList(Stereotype.RESPONSE.toString()),
                                singletonList(x.getKey()))) //
                        .orElse("")) //
                .collect(joining());

        return part1 + part2 + part3 + part4;
    }

}
