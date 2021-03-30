package com.github.davidmoten.oas3.internal;

import static com.github.davidmoten.oas3.internal.Util.first;
import static com.github.davidmoten.oas3.internal.Util.nullMapToEmpty;

import com.github.davidmoten.oas3.internal.model.Association;
import com.github.davidmoten.oas3.internal.model.Class;
import com.github.davidmoten.oas3.internal.model.ClassType;
import com.github.davidmoten.oas3.internal.model.Model;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

public final class ComponentsHelper {

    private ComponentsHelper() {
        // prevent instantiation
    }

    public static Model toModel(Names names) {
        Model part1 = names.schemas() //
                .entrySet() //
                .stream() //
                .map(entry -> Common.toModelClass(names.schemaClassName(entry.getKey()), entry.getValue(), names,
                        ClassType.SCHEMA)) //
                .reduce(Model.EMPTY, (a, b) -> a.add(b));

        Model part2 = names.requestBodies() //
                .entrySet() //
                .stream() //
                .map(entry -> {
                    RequestBody b = entry.getValue();
                    String className = names.requestBodyClassName(b);
                    String ref = b.get$ref();
                    if (ref != null) {
                        String otherClassName = names.refToClassName(ref);
                        Class c = new Class(className, ClassType.REQUEST_BODY);
                        Association a = Association.from(className).to(otherClassName).one().build();
                        return new Model(c, a);
                    } else {
                        return Common.toModelClass(names.requestBodyClassName(entry.getKey()),
                                first(entry.getValue().getContent()).get().getValue().getSchema(), names,
                                ClassType.REQUEST_BODY);
                    }
                }) //
                .reduce(Model.EMPTY, (a, b) -> a.add(b));

        Model part3 = names.parameters() //
                .entrySet() //
                .stream() //
                .map(entry -> {
                    Parameter p = entry.getValue();
                    String className = names.parameterClassName(p);
                    String ref = p.get$ref();
                    if (ref != null) {
                        Class c = new Class(className, ClassType.PARAMETER);
                        String otherClassName = names.refToClassName(ref);
                        Association a = Association.from(className).to(otherClassName).one().build();
                        return new Model(c, a);
                    } else {
                        return Common.toModelClass(className, p.getSchema(), names, ClassType.PARAMETER);
                    }
                }) //
                .reduce(Model.EMPTY, (a, b) -> a.add(b));

        Model part4 = names.responses() //
                .entrySet() //
                .stream() //
                // TODO handle ref responses as per parameters and request bodies above
                .map(entry -> first(nullMapToEmpty(entry.getValue().getContent())) //
                        .map(x -> Common.toModelClass(names.responseClassName(entry.getKey()), x.getValue().getSchema(),
                                names, ClassType.RESPONSE)) //
                        .orElse(new Model(new Class(names.responseClassName(entry.getKey()), ClassType.RESPONSE)))) //
                .reduce(Model.EMPTY, (a, b) -> a.add(b));
        return part1.add(part2).add(part3).add(part4);
    }

}
