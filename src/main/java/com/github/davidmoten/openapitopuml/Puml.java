package com.github.davidmoten.openapitopuml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.guavamini.Sets;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class Puml {

    public static String toPuml(InputStream in) throws IOException {
        return toPuml(IOUtils.toString(in, StandardCharsets.UTF_8));
    }

    public static String toPuml(String openApi) {
        SwaggerParseResult result = new OpenAPIParser().readContents(openApi, null, null);

        // or from a file
        // SwaggerParseResult result = new
        // OpenAPIParser().readContents("./path/to/openapi.yaml", null, null);

        // the parsed POJO
        OpenAPI a = result.getOpenAPI();

        return "@startuml" //
                + components(a) //
                + paths(a) //
                + "\n@enduml";
    }

    private static String paths(OpenAPI a) {
        return a.getPaths() //
                .entrySet() //
                .stream() //
                .map(entry -> toPlantUmlPath(entry.getKey(), entry.getValue())).collect(Collectors.joining());

    }

    private static String components(OpenAPI a) {
        return a.getComponents() //
                .getSchemas() //
                .entrySet() //
                .stream() //
                .map(entry -> toPlantUmlClass(entry.getKey(), entry.getValue())) //
                .collect(Collectors.joining()) //
        ;
    }

    private static String toPlantUmlPath(String name, PathItem p) {
        // add method class blocks with HTTP verb and parameters
        // add response lines
        return operations(p).stream().map(o -> "").collect(Collectors.joining());
    }

    private static List<Operation> operations(PathItem p) {
        return Stream.of(p.getGet(), p.getPost(), p.getPut(), p.getPatch()) //
                .filter(o -> o != null) //
                .collect(Collectors.toList());
    }

    private static String toPlantUmlClass(String name, Schema<?> schema) {
        StringBuilder b = new StringBuilder();
        List<Entry<String, Schema<?>>> more = new ArrayList<>();
        b.append("\n\nclass " + name + " {\n");
        List<String> relationships = new ArrayList<>();
        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            String otherClassName = refToClassName(ref);
            relationships.add(name + " --> " + otherClassName);
        } else if (schema.getProperties() != null) {
            final Set<String> required;
            if (schema.getRequired() != null) {
                required = new HashSet<>(schema.getRequired());
            } else {
                required = Collections.emptySet();
            }
            schema.getProperties().entrySet().forEach(entry -> {
                if (entry.getValue() instanceof StringSchema) {
                    append(b, required, "string", entry.getKey());
                } else if (entry.getValue() instanceof NumberSchema) {
                    append(b, required, "decimal", entry.getKey());
                } else if (entry.getValue() instanceof DateTimeSchema) {
                    append(b, required, "timestamp", entry.getKey());
                } else if (entry.getValue() instanceof DateSchema) {
                    append(b, required, "date", entry.getKey());
                } else if (entry.getValue() instanceof BooleanSchema) {
                    append(b, required, "boolean", entry.getKey());
                } else if (entry.getValue() instanceof IntegerSchema) {
                    append(b, required, "integer", entry.getKey());
                } else if (entry.getValue() instanceof ArraySchema) {
                    ArraySchema a = (ArraySchema) entry.getValue();
                    Schema<?> items = a.getItems();
                    String ref = items.get$ref();
                    if (ref != null) {
                        String otherClassName = refToClassName(ref);
                        addToMany(relationships, name, otherClassName, entry.getKey());
                    } else {
                        // TODO
                    }
                } else if (entry.getValue().get$ref() != null) {
                    String ref = entry.getValue().get$ref();
                    String otherClassName = refToClassName(ref);
                    addToOne(relationships, name, otherClassName, entry.getKey());
                }
            });
        } else if (schema instanceof ArraySchema) {
            ArraySchema a = (ArraySchema) schema;
            Schema<?> items = a.getItems();
            String ref = items.get$ref();
            if (ref != null) {
                String otherClassName = refToClassName(ref);
                addToMany(relationships, name, otherClassName);
            } else {
                // TODO
            }
        } else if (schema instanceof ObjectSchema) {
            ObjectSchema s = (ObjectSchema) schema;
            if (s.get$ref() != null) {
                String otherClassName = s.get$ref().substring(s.get$ref().lastIndexOf("/") + 1);
                relationships.add(name + " --> " + otherClassName);
            } else {
                // TODO
            }
        } else if (schema instanceof StringSchema) {
            append(b, Sets.newHashSet("value"), "string", "value");
        } else {
            // TODO
            System.out.println("not processed " + name + ":" + schema);
        }
        b.append("}");
        for (Entry<String, Schema<?>> entry : more) {
            b.append(toPlantUmlClass(entry.getKey(), entry.getValue()));
        }
        for (String relationship : relationships) {
            b.append("\n\n" + relationship);
        }
        return b.toString();
    }

    private static void addToMany(List<String> relationships, String name, String otherClassName) {
        addToMany(relationships, name, otherClassName, null);
    }

    private static void addToMany(List<String> relationships, String name, String otherClassName, String field) {
        relationships.add(name + " --> \"*\" " + otherClassName
                + (field == null || field.equals(otherClassName) ? "" : " : " + field));
    }

    private static void addToOne(List<String> relationships, String name, String otherClassName, String field) {
        relationships.add(name + " --> \"1\" " + otherClassName + (field.equals(otherClassName) ? "" : " : " + field));
    }

    private static void append(StringBuilder b, Set<String> required, String type, String name) {
        b.append("  " + name + " : " + type + required(required, name) + "\n");
    }

    private static String required(Set<String> required, String name) {
        if (required.contains(name)) {
            return "";
        } else {
            return " {O}";
        }
    }

    private static String refToClassName(String ref) {
        return ref.substring(ref.lastIndexOf("/") + 1);
    }

}
