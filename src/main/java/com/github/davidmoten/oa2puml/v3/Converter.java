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
import java.util.concurrent.atomic.AtomicLong;
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

    private static final String PATH_RELATIONSHIP_RIGHT_ARROW = " ..> ";
    private static final String CLASS_RELATIONSHIP_RIGHT_ARROW = " --> ";
    private static final String INHERITANCE_LEFT_ARROW = " <|-- ";

    private Converter() {
        // prevent instantiation
    }

    public static String openApiToPuml(InputStream in) throws IOException {
        return openApiToPuml(IOUtils.toString(in, StandardCharsets.UTF_8));
    }

    public static String openApiToPuml(String openApi) {
        AtomicLong counter = new AtomicLong();
        SwaggerParseResult result = new OpenAPIParser().readContents(openApi, null, null);

        // or from a file
        // SwaggerParseResult result = new
        // OpenAPIParser().readContents("./path/to/openapi.yaml", null, null);

        // the parsed POJO
        OpenAPI a = result.getOpenAPI();

        return "@startuml" //
                + components(a, counter) //
                + paths(a, counter) //
                + "\n\n@enduml";
    }

    private static String paths(OpenAPI a, AtomicLong counter) {
        if (a.getPaths() == null) {
            return "";
        } else {
            return "\nhide <<Method>> circle" //
                    + a.getPaths() //
                            .entrySet() //
                            .stream() //
                            .map(entry -> toPlantUmlPath(entry.getKey(), //
                                    entry.getValue(), counter))
                            .collect(Collectors.joining());
        }
    }

    private static String components(OpenAPI a, AtomicLong counter) {
        return a.getComponents() //
                .getSchemas() //
                .entrySet() //
                .stream() //
                .map(entry -> toPlantUmlClass(entry.getKey(), entry.getValue(), counter)) //
                .collect(Collectors.joining());
    }

    private static String toPlantUmlPath(String path, PathItem p, AtomicLong counter) {
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
                    s.append("\n\nclass " + quote(className) + " <<Method>> {");
                    List<Parameter> parameters = operation.getParameters();
                    if (parameters != null) {
                        s.append(parameters //
                                .stream()//
                                .map(param -> {
                                    final String type = getUmlTypeName(param.get$ref(), param.getSchema());
                                    final String optional = param.getRequired()!= null && param.getRequired() ? "" : " {O}";
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
                                if (ent.getValue().getContent() == null) {
                                    return "";
                                } else {
                                    Entry<String, MediaType> mediaType = ent.getValue().getContent().entrySet()
                                            .parallelStream().findFirst().get();
                                    Schema<?> sch = mediaType.getValue().getSchema();
                                    final String returnClassName;
                                    final String returnClassDeclaration;
                                    if (sch.get$ref() != null) {
                                        returnClassName = refToClassName(sch.get$ref());
                                        returnClassDeclaration = "";
                                    } else {
                                        returnClassName = (className + " " + responseCode + " Return");
                                        returnClassDeclaration = toPlantUmlClass(returnClassName, sch, counter);
                                    }
                                    return returnClassDeclaration + "\n\n" + quote(className)
                                            + PATH_RELATIONSHIP_RIGHT_ARROW + quote(returnClassName) + ": "
                                            + responseCode;
                                }
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

    private static String toPlantUmlClass(String name, Schema<?> schema, AtomicLong counter) {
        StringBuilder b = new StringBuilder();
        List<Entry<String, Schema<?>>> more = new ArrayList<>();
        b.append("\n\nclass " + quote(name) + " {\n");
        List<String> relationships = new ArrayList<>();
        if (schema.get$ref() != null) {
            // this is an alias case for a schema
            String ref = schema.get$ref();
            String otherClassName = refToClassName(ref);
            relationships.add(quote(name) + CLASS_RELATIONSHIP_RIGHT_ARROW + quote(otherClassName));
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema s = (ComposedSchema) schema;
            if (s.getOneOf() != null) {
                validateComposed(s.getOneOf());
                addInheritance(relationships, name, s.getOneOf(), null, counter);
            } else if (s.getAnyOf() != null) {
                validateComposed(s.getAnyOf());
                addInheritance(relationships, name, s.getAnyOf(), null, counter);
            } else if (s.getAllOf() != null) {
//                validateComposed(s.getAllOf());
                addInheritance(relationships, name, s.getAllOf(), Cardinality.ALL, counter);
            }
        } else if (schema.getProperties() != null) {
            final Set<String> required;
            if (schema.getRequired() != null) {
                required = new HashSet<>(schema.getRequired());
            } else {
                required = Collections.emptySet();
            }
            schema.getProperties().entrySet().forEach(entry -> {
                if (entry.getValue() instanceof ComposedSchema) {
                    ComposedSchema s = (ComposedSchema) entry.getValue();
                    @SuppressWarnings("rawtypes")
                    final List<Schema> list;
                    final Cardinality cardinality;
                    boolean req = required.contains(entry.getKey());
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
                        addInheritanceForProperty(relationships, name, list, entry.getKey(), counter, cardinality);
                    }
                } else if (entry.getValue().get$ref() != null) {
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
                throw new RuntimeException("class level array of non-ref type not supported yet");
            }
        } else if (schema instanceof ObjectSchema) {
            // has no properties so ignore
        } else {
            String type = getUmlTypeName(schema.get$ref(), schema);
            append(b, Sets.newHashSet("value"),type, name);
            //TODO handle Arrays (add anon classes if required)
        } 
        b.append("}");
        for (Entry<String, Schema<?>> entry : more) {
            b.append(toPlantUmlClass(entry.getKey(), entry.getValue(), counter));
        }
        for (String relationship : relationships) {
            b.append("\n\n" + relationship);
        }
        return b.toString();
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

    private static void addInheritanceForProperty(List<String> relationships, String name,
            @SuppressWarnings("rawtypes") List<Schema> schemas, String propertyName, AtomicLong counter,
            Cardinality cardinality) {
        String label = "anon" + counter.incrementAndGet();
        relationships.add("diamond " + label);
        relationships.add(quote(name) + CLASS_RELATIONSHIP_RIGHT_ARROW + "\"" + cardinality + "\" " + label + ": "
                + propertyName);
        List<String> otherClassNames = addAnonymousClassesAndReturnOtherClassNames(relationships, schemas, counter);
        for (String otherClassName : otherClassNames) {
            relationships.add(label + INHERITANCE_LEFT_ARROW + quote(otherClassName));
        }
    }

    private static void addInheritance(List<String> relationships, String name,
            @SuppressWarnings("rawtypes") List<Schema> schemas, Cardinality cardinality, AtomicLong counter) {
        List<String> otherClassNames = addAnonymousClassesAndReturnOtherClassNames(relationships, schemas, counter);
        final String s = cardinality == null ? "" : " \"" + cardinality + "\"";
        for (String otherClassName : otherClassNames) {
            relationships.add(quote(name) + s + INHERITANCE_LEFT_ARROW + quote(otherClassName));
        }
    }

    private static List<String> addAnonymousClassesAndReturnOtherClassNames(List<String> relationships, List<Schema> schemas, AtomicLong counter) {
        List<String> otherClassNames = schemas.stream() //
                .map(s -> {
                    if (s.get$ref() != null) {
                        return refToClassName(s.get$ref());
                    } else {
                        String className = "anon" + counter.incrementAndGet();
                        String classDeclaration = toPlantUmlClass(className, s, counter);
                        relationships.add(classDeclaration);
                        return className;
                    }
                }).collect(Collectors.toList());
        return otherClassNames;
    }

    private static void validateComposed(@SuppressWarnings("rawtypes") List<Schema> schemas) {
        if (schemas.stream().anyMatch(s -> s.get$ref() == null)) {
            throw new RuntimeException(
                    "all elements of a composed type (oneOf, etc.) must be $ref (so that a meaningful diagram can be generated).\n"
                            + schemas.stream().map(s -> s.toString()).collect(Collectors.joining("\n")));
        }
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

    private static void addToOne(List<String> relationships, String name, String otherClassName, String field,
            boolean isToOne) {
        relationships.add(quote(name) + CLASS_RELATIONSHIP_RIGHT_ARROW + "\"" + (isToOne ? "1" : "0..1") + "\" "
                + quote(otherClassName) + (field.equals(otherClassName) ? "" : " : " + field));
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
