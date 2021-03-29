package com.github.davidmoten.oas3.puml2;

import static com.github.davidmoten.oas3.puml.Util.first;
import static com.github.davidmoten.oas3.puml.Util.nullMapToEmpty;

import com.github.davidmoten.oas3.model.Association;
import com.github.davidmoten.oas3.model.Class;
import com.github.davidmoten.oas3.model.ClassType;
import com.github.davidmoten.oas3.model.Model;
import com.github.davidmoten.oas3.puml.Names;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

public final class ComponentsHelper {

    static Model toModel(Names names) {
        Model part1 = names.schemas() //
                .entrySet() //
                .stream() //
                .map(entry -> Common.toPlantUmlClass(names.schemaClassName(entry.getKey()),
                        entry.getValue(), names)) //
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
                        Association a = Association.from(className).to(otherClassName).one()
                                .build();
                        return new Model(c, a);
                    } else {
                        return Common.toPlantUmlClass(names.requestBodyClassName(entry.getKey()),
                                first(entry.getValue().getContent()).get().getValue().getSchema(),
                                names, Stereotype.REQUEST_BODY);
                    }
                }) //
                .reduce(Model.EMPTY,(a, b) -> a.add(b));

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
                        return Common.toPlantUmlClass(className, p.getSchema(), names,
                                Stereotype.PARAMETER);
                    }
                }) //
                .reduce(Model.EMPTY,(a,b) -> a.add(b));

        Model part4 = names.responses() //
                .entrySet() //
                .stream() //
                // TODO handle ref responses as per parameters and request bodies above
                .map(entry -> first(nullMapToEmpty(entry.getValue().getContent())) //
                        .map(x -> Common.toPlantUmlClass(names.responseClassName(entry.getKey()),
                                x.getValue().getSchema(), names,
                                Stereotype.RESPONSE)) //
                        .orElse(Model.EMPTY)) //
                .reduce(Model.EMPTY,(a,b)-> a.add(b));
        return part1.add(part2).add(part3).add(part4);
    }
    
}
