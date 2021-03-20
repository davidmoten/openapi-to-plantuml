package com.github.davidmoten.oa2puml.v3;

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

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.guavamini.Sets;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Converter {

    private Converter() {
        // prevent instantiation
    }

    public static String openApiToPuml(InputStream in) throws IOException {
        return openApiToPuml(IOUtils.toString(in, StandardCharsets.UTF_8));
    }

    public static String openApiToPuml(String openApi) {
        SwaggerParseResult result = new OpenAPIParser().readContents(openApi, null, null);

        // or from a file
        // SwaggerParseResult result = new
        // OpenAPIParser().readContents("./path/to/openapi.yaml", null, null);

        // the parsed POJO
        OpenAPI a = result.getOpenAPI();

        return "@startuml" //
                + components(a) //
                + paths(a) //
                + "\n\n@enduml";
    }

    private static String paths(OpenAPI a) {
        if (a.getPaths() == null) {
            return "";
        } else {
            return "\nhide <<Method>> circle" //
                    + a.getPaths() //
                            .entrySet() //
                            .stream() //
                            .map(entry -> toPlantUmlPath(entry.getKey(), //
                                    entry.getValue()))
                            .collect(Collectors.joining());
        }
    }

    private static String components(OpenAPI a) {
        return a.getComponents() //
                .getSchemas() //
                .entrySet() //
                .stream() //
                .map(entry -> toPlantUmlClass(entry.getKey(), entry.getValue())) //
                .collect(Collectors.joining());
    }

    private static String toPlantUmlPath(String path, PathItem p) {
        StringBuilder b = new StringBuilder();
        // add method class blocks with HTTP verb and parameters
        // add response lines
        b.append(p.readOperationsMap() //
                .entrySet() //
                .stream() //
                .map(entry -> {
                    Operation operation = entry.getValue();
                    String className = entry.getKey() + " " + path;
                    StringBuilder s = new StringBuilder();
                    s.append("\n\nclass \"" + className + "\" <<Method>> {");
                    List<Parameter> parameters = operation.getParameters();
                    if (parameters != null) {
                        s.append(parameters //
                                .stream()//
                                .map(param -> {
                                    final String type = getUmlTypeName(param.get$ref(), param.getSchema());
                                    final String optional = param.getRequired() ? "" : " {O}";
                                    return "\n" + "  " + param.getName() + " : " + type + optional;
                                }) //
                                .collect(Collectors.joining()));
                    }
                    s.append("\n}");
                    s.append(operation //
                            .getResponses() //
                            .entrySet() //
                            .stream() //
                            .map(ent -> {
                                String responseCode = ent.getKey();
                                // TODO only using the first content
                                Entry<String, MediaType> mediaType = ent.getValue().getContent().entrySet()
                                        .parallelStream().findFirst().get();
                                String returnClassName = refToClassName(mediaType.getValue().getSchema().get$ref());
                                return "\n\n\"" + className + "\" --> \"" + returnClassName + "\": " + responseCode;
                            }).collect(Collectors.joining()));
                    return s.toString();
                }) //
                .collect(Collectors.joining()));
        return b.toString();
    }

    private static String getUmlTypeName(String ref, Schema<?> schema) {
        final String type;
        if (ref != null) {
            type = refToClassName(ref);
        } else if (schema instanceof StringSchema) {
            type = "string";
        } else if (schema instanceof BooleanSchema) {
            type = "boolean";
        } else if (schema instanceof DateTimeSchema) {
            type = "timestamp";
        } else if (schema instanceof DateSchema) {
            type = "date";
        } else if (schema instanceof NumberSchema) {
            type = "decimal";
        } else if (schema instanceof IntegerSchema) {
            type = "integer";
        } else if (schema instanceof ArraySchema) {
            ArraySchema a = (ArraySchema) schema;
            type = getUmlTypeName(schema.get$ref(), a.getItems()) + "[]";
        } else if (schema instanceof BinarySchema) {
            type = "byte[]";
        } else {
            type = "unknown";
        }
        return type;
    }

    private static String toPlantUmlClass(String name, Schema<?> schema) {
        StringBuilder b = new StringBuilder();
        List<Entry<String, Schema<?>>> more = new ArrayList<>();
        b.append("\n\nclass " + name + " {\n");
        List<String> relationships = new ArrayList<>();
        if (schema.get$ref() != null) {
            // this is an alias case for a schema
            String ref = schema.get$ref();
            String otherClassName = refToClassName(ref);
            relationships.add(name + " --> " + otherClassName);
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema s = (ComposedSchema) schema;
            System.out.println(s);
            if (!s.getOneOf().isEmpty()) {
                validateComposed(s.getOneOf());
                List<String> otherClassNames = s //
                        .getOneOf() //
                        .stream() //
                        .map(x -> refToClassName(x.get$ref())) //
                        .collect(Collectors.toList());
                addInheritance(relationships, name, otherClassNames);
            }
        } else if (schema.getProperties() != null) {
            final Set<String> required;
            if (schema.getRequired() != null) {
                required = new HashSet<>(schema.getRequired());
            } else {
                required = Collections.emptySet();
            }
            schema.getProperties().entrySet().forEach(entry -> {
                if (entry.getValue().get$ref() != null) {
                    String ref = entry.getValue().get$ref();
                    String otherClassName = refToClassName(ref);
                    addToOne(relationships, name, otherClassName, entry.getKey(), required.contains(entry.getKey()));
                } else {
                    String type = getUmlTypeName(entry.getValue().get$ref(), entry.getValue());
                    if (type.endsWith("[]") && !type.equals("byte[]")) {
                        // is array of items
                        ArraySchema a = (ArraySchema) entry.getValue();
                        Schema<?> items = a.getItems();
                        String ref = items.get$ref();
                        if (ref != null) {
                            String otherClassName = refToClassName(ref);
                            addToMany(relationships, name, otherClassName, entry.getKey());
                        } else {
                            append(b, required, type, entry.getKey());
                        }
                    } else {
                        append(b, required, type, entry.getKey());
                    }
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
            throw new RuntimeException("anonymous object schema fragements not supported");
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

    private static void addInheritance(List<String> relationships, String name, List<String> otherClassNames) {
        for (String otherClassName : otherClassNames) {
            relationships.add(name + " <|-- " + otherClassName);
        }
    }

    private static void validateComposed(@SuppressWarnings("rawtypes") List<Schema> schemas) {
        if (schemas.stream().anyMatch(s -> s.get$ref() == null)) {
            System.out.println(schemas.stream().map(x -> x.getClass().getSimpleName()).collect(Collectors.toList()));
            throw new RuntimeException(
                    "all elements of a composed type (oneOf, etc.) must be $ref (so that a meaningful diagram can be generated).\n"
                            + schemas.stream().map(s -> s.toString()).collect(Collectors.joining("\n")));
        }

    }

    private static void addToMany(List<String> relationships, String name, String otherClassName) {
        addToMany(relationships, name, otherClassName, null);
    }

    private static void addToMany(List<String> relationships, String name, String otherClassName, String field) {
        relationships.add(name + " --> \"*\" " + otherClassName
                + (field == null || field.equals(otherClassName) ? "" : " : " + field));
    }

    private static void addToOne(List<String> relationships, String name, String otherClassName, String field,
            boolean isToOne) {
        relationships.add(name + " --> \"" + (isToOne ? "1" : "0..1") + "\" " + otherClassName
                + (field.equals(otherClassName) ? "" : " : " + field));
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
