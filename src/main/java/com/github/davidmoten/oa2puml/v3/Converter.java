package com.github.davidmoten.oa2puml.v3;

import static com.github.davidmoten.oa2puml.v3.Util.nullToEmpty;

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
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Converter {

    private static final String PATH_RELATIONSHIP_RIGHT_ARROW = " ..> ";
    private static final String CLASS_RELATIONSHIP_RIGHT_ARROW = " --> ";
    private static final String INHERITANCE_LEFT_ARROW = " <|-- ";
    private static final Set<String> simpleTypesWithoutBrackets = Sets.newHashSet("string", "decimal", "integer",
            "byte", "date", "boolean");

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
        Names names = new Names(a);
        return "@startuml" //
                + components(a, names) //
                + paths(a, names) //
                + "\n\n@enduml";
    }

    private static String paths(OpenAPI a, Names names) {
        if (a.getPaths() == null) {
            return "";
        } else {
            return "\nhide <<Method>> circle" //
                    + a.getPaths() //
                            .entrySet() //
                            .stream() //
                            .map(entry -> toPlantUmlPath(a, entry.getKey(), //
                                    entry.getValue(), names))
                            .collect(Collectors.joining());
        }
    }

    private static String components(OpenAPI a, Names names) {
        String part1 = nullToEmpty(a.getComponents().getSchemas()) //
                .entrySet() //
                .stream() //
                .map(entry -> toPlantUmlClass(entry.getKey(), entry.getValue(), names)) //
                .collect(Collectors.joining());

        String part2 = nullToEmpty(a.getComponents().getRequestBodies()) //
                .entrySet() //
                .stream() //
                .map(entry -> toPlantUmlClass(entry.getKey(),
                        entry.getValue().getContent().entrySet().stream().findFirst().get().getValue().getSchema(),
                        names)) //
                .collect(Collectors.joining());

        String part3 = nullToEmpty(a.getComponents().getParameters()) //
                .entrySet() //
                .stream() //
                .map(entry -> toPlantUmlClass(entry.getKey(), entry.getValue().getSchema(), names)) //
                .collect(Collectors.joining());

        return part1 + part2 + part3;
    }

    private static String toPlantUmlPath(OpenAPI a, String path, PathItem p, Names names) {
        StringBuilder b = new StringBuilder();
        StringBuilder extras = new StringBuilder();
        // add method class blocks with HTTP verb and parameters
        // add response lines
        b.append(p.readOperationsMap() //
                .entrySet() //
                .stream() //
                .map(entry -> {
                    Operation operation = entry.getValue();
                    String className = entry.getKey() + " " + path;
                    StringBuilder s = new StringBuilder();
                    s.append("\n\nclass " + quote(className) + " <<Method>> {");
                    List<Parameter> parameters = operation.getParameters();
                    int[] parameterNo = new int[1];
                    if (parameters != null) {
                        s.append(parameters //
                                .stream()//
                                .map(param -> {
                                    parameterNo[0]++;
                                    String parameterName = param.getName() == null ? "parameter" + parameterNo[0]
                                            : param.getName();
                                    if (param.getSchema() != null) {
                                        toPlantUmlClass(className + "." + parameterName, param.getSchema(), names);
                                    }
                                    final String type = getUmlTypeName(param.get$ref(), param.getSchema(), names);
                                    if (isSimpleType(type)) {
                                        final String optional = param.getRequired() != null && param.getRequired() ? ""
                                                : " {O}";
                                        return "\n" + "  " + parameterName + " : " + type + optional;
                                    } else {
                                        extras.append("\n\n" + quote(className) + CLASS_RELATIONSHIP_RIGHT_ARROW
                                                + quote("1") + quote(type) + " : " + quote(parameterName));
                                        return "";
                                    }
                                }) //
                                .collect(Collectors.joining()));
                    }
                    s.append("\n}");
                    s.append(operation //
                            .getResponses() //
                            .entrySet() //
                            .stream() //
                            .map(ent -> {
                                // TODO support refs to #/components/responses
                                String responseCode = ent.getKey();
                                // TODO only using the first content
                                ApiResponse r = ent.getValue();
                                if (r.get$ref() != null) {
                                    r = a.getComponents().getResponses().get(names.refToClassName(r.get$ref()));
                                }
                                if (r.getContent() == null) {
                                    return "";
                                } else {
                                    Entry<String, MediaType> mediaType = r.getContent().entrySet().stream().findFirst()
                                            .get();
                                    Schema<?> sch = mediaType.getValue().getSchema();
                                    final String returnClassName;
                                    final String returnClassDeclaration;
                                    if (sch != null && sch.get$ref() != null) {
                                        returnClassName = names.refToClassName(sch.get$ref());
                                        returnClassDeclaration = "";
                                    } else {
                                        returnClassName = (className + " " + responseCode + " Return");
                                        if (sch == null) {
                                            returnClassDeclaration = "";
                                        } else {
                                            returnClassDeclaration = toPlantUmlClass(returnClassName, sch, names);
                                        }
                                    }
                                    return returnClassDeclaration + "\n\n" + quote(className)
                                            + PATH_RELATIONSHIP_RIGHT_ARROW + quote(returnClassName) + ": "
                                            + responseCode;
                                }
                            }).collect(Collectors.joining()));
                    // TODO support RequestBody (post)
                    RequestBody body = operation.getRequestBody();
                    if (body != null) {
                        Content content = body.getContent();
                        if (content != null) {
                            Entry<String, MediaType> mediaType = content.entrySet().stream().findFirst().get();
                            // use the first content entry
                            final String requestBodyClassName;
                            final String requestBodyClassDeclaration;
                            Schema<?> sch = mediaType.getValue().getSchema();
                            if (sch != null && sch.get$ref() != null) {
                                requestBodyClassName = names.refToClassName(sch.get$ref());
                                requestBodyClassDeclaration = "";
                            } else {
                                requestBodyClassName = className + " Request";
                                if (sch == null) {
                                    requestBodyClassDeclaration = "";
                                } else {
                                    requestBodyClassDeclaration = toPlantUmlClass(requestBodyClassName, sch, names);
                                }
                            }
                            s.append(requestBodyClassDeclaration + "\n\n" + quote(className)
                                    + CLASS_RELATIONSHIP_RIGHT_ARROW + quote(requestBodyClassName) + " : "
                                    + quote("<<Request Body>>"));
                        }
                    }
                    return s.toString();
                }) //
                .collect(Collectors.joining()));
        b.append(extras.toString());
        return b.toString();
    }

    private static String getUmlTypeName(String ref, Schema<?> schema, Names names) {
        final String type;
        if (ref != null) {
            type = names.refToClassName(ref);
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
            type = getUmlTypeName(a.getItems().get$ref(), a.getItems(), names) + "[]";
        } else if (schema instanceof BinarySchema) {
            type = "byte[]";
        } else if (schema instanceof ObjectSchema) {
            type = "object";
        } else if (schema instanceof MapSchema) {
            // TODO handle MapSchema
            return "map";
        } else if (schema instanceof ComposedSchema) {
            // TODO handle ComposedSchema
            return "composed";
        } else if ("string".equals(schema.getType())) {
            type = "string";
        } else if (schema.get$ref() != null) {
            type = names.refToClassName(schema.get$ref());
        } else if (schema.getType() == null) {
            // TODO don't display a type with empty
            type = "empty";
        } else {
            throw new RuntimeException("not expected" + schema);
        }
        return type;
    }

    private static String toPlantUmlClass(String name, Schema<?> schema, Names names) {
        StringBuilder b = new StringBuilder();
        List<Entry<String, Schema<?>>> more = new ArrayList<>();
        b.append("\n\nclass " + quote(name) + " {\n");
        List<String> relationships = new ArrayList<>();
        if (schema.get$ref() != null) {
            // this is an alias case for a schema
            String otherClassName = names.refToClassName(schema.get$ref());
            relationships.add(quote(name) + CLASS_RELATIONSHIP_RIGHT_ARROW + "\"1\"" + quote(otherClassName));
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema s = (ComposedSchema) schema;
            if (s.getOneOf() != null) {
                addInheritance(relationships, name, s.getOneOf(), null, names);
            } else if (s.getAnyOf() != null) {
                addInheritance(relationships, name, s.getAnyOf(), null, names);
            } else if (s.getAllOf() != null) {
                addMixedTypeAll(relationships, name, s.getAllOf(), null, names);
            } else {
                throw new RuntimeException("unexpected");
            }
        } else if (schema.getProperties() != null) {
            final Set<String> required;
            if (schema.getRequired() != null) {
                required = new HashSet<>(schema.getRequired());
            } else {
                required = Collections.emptySet();
            }
            schema.getProperties().entrySet().forEach(entry -> {
                String property = entry.getKey();
                if (entry.getValue() instanceof ComposedSchema) {
                    ComposedSchema s = (ComposedSchema) entry.getValue();
                    @SuppressWarnings("rawtypes")
                    final List<Schema> list;
                    final Cardinality cardinality;
                    boolean req = required.contains(property);
                    if (s.getOneOf() != null) {
                        list = s.getOneOf();
                        cardinality = req ? Cardinality.ONE : Cardinality.ZERO_ONE;
                    } else if (s.getAnyOf() != null) {
                        list = s.getAnyOf();
                        cardinality = req ? Cardinality.ONE : Cardinality.ZERO_ONE;
                    } else if (s.getAllOf() != null) {
                        list = s.getAllOf();
                        cardinality = Cardinality.ALL;
                    } else {
                        list = Collections.emptyList();
                        cardinality = null;
                    }
                    if (!list.isEmpty()) {
                        if (cardinality == Cardinality.ALL) {
                            addMixedTypeAll(relationships, name, list, property, names);
                        } else {
                            addInheritanceForProperty(relationships, name, list, property, cardinality, names);
                        }
                    }
                } else if (entry.getValue().get$ref() != null) {
                    String ref = entry.getValue().get$ref();
                    String otherClassName = names.refToClassName(ref);
                    addToOne(relationships, name, otherClassName, property, required.contains(entry.getKey()));
                } else {
                    String type = getUmlTypeName(entry.getValue().get$ref(), entry.getValue(), names);
                    if (type.startsWith("unknown")) {
                        System.out.println("unknown property:\n" + entry);
                    }
                    if (isComplexArrayType(type)) {
                        addArray(name, relationships, property, entry.getValue(), names);
                    } else if (type.equals("object")) {
                        // create anon class
                        String otherClassName = names.nextClassName(name + "." + property);
                        relationships.add(toPlantUmlClass(otherClassName, entry.getValue(), names).trim());
                        addToOne(relationships, name, otherClassName, property, required.contains(property));
                    } else {
                        append(b, required, type, entry.getKey());
                    }
                }
            });
        } else if (schema instanceof ArraySchema) {
            ArraySchema a = (ArraySchema) schema;
            Schema<?> items = a.getItems();
            String ref = items.get$ref();
            String otherClassName;
            if (ref != null) {
                otherClassName = names.refToClassName(ref);
            } else {
                // create anon class
                otherClassName = names.nextClassName(name);
                relationships.add(toPlantUmlClass(otherClassName, items, names).trim());
            }
            addToMany(relationships, name, otherClassName);
        } else if (schema instanceof ObjectSchema) {
            // has no properties so ignore
        } else {
            String type = getUmlTypeName(schema.get$ref(), schema, names);
            if (isComplexArrayType(type)) {
                addArray(name, relationships, null, schema, names);
            } else {
                append(b, Sets.newHashSet("value"), type, "value");
            }
        }
        b.append("}");
        for (Entry<String, Schema<?>> entry : more) {
            b.append(toPlantUmlClass(entry.getKey(), entry.getValue(), names));
        }
        for (String relationship : relationships) {
            b.append("\n\n" + relationship);
        }
        return b.toString();
    }

    private static boolean isComplexArrayType(String type) {
        return type.endsWith("[]") && !isSimpleType(type);
    }

    private static boolean isSimpleType(String s) {
        return simpleTypesWithoutBrackets.contains(s.replace("[", "").replace("]", ""));
    }

    private static void addArray(String name, List<String> relationships, String property,
            @SuppressWarnings("rawtypes") Schema schema, Names names) {
        // is array of items
        ArraySchema a = (ArraySchema) schema;
        Schema<?> items = a.getItems();
        String ref = items.get$ref();
        final String otherClassName;
        if (ref != null) {
            otherClassName = names.refToClassName(ref);
        } else {
            // create anon class
            otherClassName = names.nextClassName(name + (property == null ? "" : "." + property));
            relationships.add(toPlantUmlClass(otherClassName, items, names).trim());
        }
        addToMany(relationships, name, otherClassName, property);
    }

    private enum Cardinality {
        ZERO_ONE("0..1"), ONE("1"), MANY("*"), ALL("all");
        private final String string;

        private Cardinality(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    private static void addMixedTypeAll(List<String> relationships, String name,
            @SuppressWarnings("rawtypes") List<Schema> schemas, String propertyName, Names names) {
        List<String> otherClassNames = addAnonymousClassesAndReturnOtherClassNames(relationships, name, schemas, names,
                propertyName);
        for (String otherClassName : otherClassNames) {
            addToOne(relationships, name, otherClassName, propertyName, true);
        }
    }

    private static void addInheritanceForProperty(List<String> relationships, String name,
            @SuppressWarnings("rawtypes") List<Schema> schemas, String propertyName, Cardinality cardinality,
            Names names) {
        String label = names.nextClassName("anon");
        relationships.add("diamond " + label);
        relationships.add(quote(name) + CLASS_RELATIONSHIP_RIGHT_ARROW + "\"" + cardinality + "\" " + label + ": "
                + propertyName);
        List<String> otherClassNames = addAnonymousClassesAndReturnOtherClassNames(relationships, name, schemas, names,
                propertyName);
        for (String otherClassName : otherClassNames) {
            relationships.add(label + INHERITANCE_LEFT_ARROW + quote(otherClassName));
        }
    }

    private static void addInheritance(List<String> relationships, String name,
            @SuppressWarnings("rawtypes") List<Schema> schemas, Cardinality cardinality, Names names) {
        List<String> otherClassNames = addAnonymousClassesAndReturnOtherClassNames(relationships, name, schemas, names,
                null);
        final String s = cardinality == null ? "" : " \"" + cardinality + "\"";
        for (String otherClassName : otherClassNames) {
            relationships.add(quote(name) + s + INHERITANCE_LEFT_ARROW + quote(otherClassName));
        }
    }

    private static List<String> addAnonymousClassesAndReturnOtherClassNames(List<String> relationships, String name,
            @SuppressWarnings("rawtypes") List<Schema> schemas, Names names, String property) {
        List<String> otherClassNames = schemas.stream() //
                .map(s -> {
                    if (s.get$ref() != null) {
                        return names.refToClassName(s.get$ref());
                    } else {
                        String className = names.nextClassName(name + (property == null ? "" : "." + property));
                        String classDeclaration = toPlantUmlClass(className, s, names);
                        relationships.add(classDeclaration);
                        return className;
                    }
                }).collect(Collectors.toList());
        return otherClassNames;
    }

    private static void addToMany(List<String> relationships, String name, String otherClassName) {
        addToMany(relationships, name, otherClassName, null);
    }

    private static String quote(String s) {
        return "\"" + s + "\"";
    }

    private static void addToMany(List<String> relationships, String name, String otherClassName, String field) {
        relationships.add(quote(name) + CLASS_RELATIONSHIP_RIGHT_ARROW + "\"*\" " + quote(otherClassName)
                + (field == null || field.equals(otherClassName) ? "" : " : " + field));
    }

    private static void addToOne(List<String> relationships, String name, String otherClassName, String property,
            boolean isToOne) {
        relationships.add(quote(name) + CLASS_RELATIONSHIP_RIGHT_ARROW + "\"" + (isToOne ? "1" : "0..1") + "\" "
                + quote(otherClassName)
                + (property == null || property.equals(otherClassName) ? "" : " : " + property));
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

}
