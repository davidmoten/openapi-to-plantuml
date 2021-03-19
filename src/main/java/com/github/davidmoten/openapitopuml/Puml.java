package com.github.davidmoten.openapitopuml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class Puml {

    public static String toPuml(String openApi) {
        SwaggerParseResult result = new OpenAPIParser().readContents(openApi, null, null);

        // or from a file
        // SwaggerParseResult result = new
        // OpenAPIParser().readContents("./path/to/openapi.yaml", null, null);

        // the parsed POJO
        OpenAPI a = result.getOpenAPI();

        // @startuml
//        class A {
//            {static} int counter
//            +void {abstract} start(int timeout)
//            }
//            note right of A::counter
//              This member is annotated
//            end note
//            note right of A::start
//              This method is now explained in a UML note
//            end note
//            @enduml
        return "@startuml\n" //
                + a.getComponents() //
                        .getSchemas() //
                        .entrySet() //
                        .stream() //
                        .map(entry -> toPlantUmlClass(entry.getKey(), entry.getValue())) //
                        .collect(Collectors.joining("\n\n")) //
                + "\n@enduml";
    }

    private static String toPlantUmlClass(String name, Schema<?> schema) {
        StringBuilder b = new StringBuilder();
        List<Entry<String, Schema>> more = new ArrayList<>();
        b.append("class " + name + "{\n");
        if (schema instanceof ObjectSchema) {
            ObjectSchema s = (ObjectSchema) schema;
            s.getProperties().entrySet().forEach(entry -> {
                if (entry.getValue() instanceof StringSchema) {
                    b.append("String " + entry.getKey() + "\n");
                }
                more.add(entry);
            });
        } else if (schema instanceof StringSchema) {
            StringSchema s = (StringSchema) schema;
            b.append("  String value\n");
        }
        b.append("}");
        for (Entry<String, Schema> entry: more) {
            b.append("\n");
            b.append(toPlantUmlClass(entry.getKey(), entry.getValue()));
            b.append("\n");
        }
        return b.toString();
    }

}
