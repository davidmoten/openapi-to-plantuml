package com.github.davidmoten.oas3.internal;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;
import com.github.davidmoten.oas3.internal.model.Association;
import com.github.davidmoten.oas3.internal.model.AssociationType;
import com.github.davidmoten.oas3.internal.model.Class;
import com.github.davidmoten.oas3.internal.model.ClassType;
import com.github.davidmoten.oas3.internal.model.Field;
import com.github.davidmoten.oas3.internal.model.Inheritance;
import com.github.davidmoten.oas3.internal.model.Model;
import com.github.davidmoten.oas3.internal.model.Relationship;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.PasswordSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;

public final class Common {

    private Common() {
        // prevent instantiation
    }

    private static final Set<String> SIMPLE_TYPES_WITHOUT_BRACKETS = Sets.newHashSet("string",
            "decimal", "integer", "byte", "date", "boolean", "timestamp");

    static Model toModelClass(String name, Schema<?> schema, Names names, ClassType classType) {
        List<Field> fields = new ArrayList<>();

        List<Relationship> relationships = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        if (schema.get$ref() != null) {
            // this is an alias case for a schema
            String otherClassName = names.refToClassName(schema.get$ref());
            relationships.add(Association.from(name).to(otherClassName).one().build());
        } else if (schema instanceof ComposedSchema) {
            ComposedSchema s = (ComposedSchema) schema;
            if (s.getOneOf() != null) {
                addInheritance(classes, relationships, name, s.getOneOf(), names);
            } else if (s.getAnyOf() != null) {
                addInheritance(classes, relationships, name, s.getAnyOf(), names);
            } else if (s.getAllOf() != null) {
                addMixedTypeAll(classes, relationships, name, s.getAllOf(), null, names);
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
                Schema<?> sch = entry.getValue();
                if (sch instanceof ComposedSchema) {
                    ComposedSchema s = (ComposedSchema) sch;
                    @SuppressWarnings("rawtypes")
                    final List<Schema> list;
                    final AssociationType associationType;
                    boolean req = required.contains(property);
                    boolean isAll = false;
                    if (s.getOneOf() != null) {
                        list = s.getOneOf();
                        associationType = req ? AssociationType.ONE : AssociationType.ZERO_ONE;
                    } else if (s.getAnyOf() != null) {
                        list = s.getAnyOf();
                        associationType = req ? AssociationType.ONE : AssociationType.ZERO_ONE;
                    } else if (s.getAllOf() != null) {
                        list = s.getAllOf();
                        isAll = true;
                        associationType = null;
                    } else {
                        // don't expect this path but be defensive
                        list = emptyList();
                        associationType = null;
                    }
                    if (!list.isEmpty()) {
                        if (isAll) {
                            addMixedTypeAll(classes, relationships, name, list, property, names);
                        } else {
                            addInheritanceForProperty(classes, relationships, name, list, property,
                                    associationType, names);
                        }
                    }
                } else if (sch.get$ref() != null) {
                    String ref = sch.get$ref();
                    String otherClassName = names.refToClassName(ref);
                    addToOne(relationships, name, otherClassName, property,
                            required.contains(entry.getKey()));
                } else {
                    Optional<String> t = getUmlTypeName(sch, names);
                    if (t.isPresent()) {
                        String type = t.get();
                        if (isComplexArrayType(type)) {
                            addArray(name, classes, relationships, property, (ArraySchema) sch,
                                    names);
                        } else if (type.equals("object")) {
                            // create anon class
                            String otherClassName = names.nextClassName(name + "." + property);
                            Model m = toModelClass(otherClassName, sch, names, classType);
                            classes.addAll(m.classes());
                            relationships.addAll(m.relationships());
                            addToOne(relationships, name, otherClassName, property,
                                    required.contains(property));
                        } else {
                            fields.add(new Field(entry.getKey(), type, type.endsWith("]"),
                                    required.contains(entry.getKey())));
                        }
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
                Model m = toModelClass(otherClassName, items, names, classType);
                classes.addAll(m.classes());
                relationships.addAll(m.relationships());
            }
            addToMany(relationships, name, otherClassName);
        } else if (!(schema instanceof ObjectSchema)) {
            // has no properties so ignore ObjectSchema
            Optional<String> t = getUmlTypeName(schema, names);
            if (t.isPresent()) {
                String type = t.get();
                fields.add(new Field("value", type, type.endsWith("]"), true));
            }
        }
        classes.add(new Class(name, classType, fields));
        return new Model(classes, relationships);
    }

    private static boolean isComplexArrayType(String type) {
        return type.endsWith("[]") && !isSimpleType(type);
    }

    static boolean isSimpleType(String s) {
        return SIMPLE_TYPES_WITHOUT_BRACKETS.contains(s.replace("[", "").replace("]", ""));
    }

    private static void addArray(String name, List<Class> classes, List<Relationship> relationships,
            String property, ArraySchema a, Names names) {
        Preconditions.checkNotNull(property);
        // is array of items
        Schema<?> items = a.getItems();
        String ref = items.get$ref();
        final String otherClassName;
        if (ref != null) {
            otherClassName = names.refToClassName(ref);
        } else {
            // create anon class
            otherClassName = names.nextClassName(name + "." + property);
            Model m = toModelClass(otherClassName, items, names, ClassType.SCHEMA);
            classes.addAll(m.classes());
            relationships.addAll(m.relationships());
        }
        addToMany(relationships, name, otherClassName, property);
    }

    private static void addMixedTypeAll(List<Class> classes, List<Relationship> relationships,
            String name, @SuppressWarnings("rawtypes") List<Schema> schemas, String propertyName,
            Names names) {
        List<String> otherClassNames = addAnonymousClassesAndReturnOtherClassNames(classes,
                relationships, name, schemas, names, propertyName);
        for (String otherClassName : otherClassNames) {
            addToOne(relationships, name, otherClassName, propertyName, true);
        }
    }

    private static void addInheritanceForProperty(List<Class> classes,
            List<Relationship> relationships, String name,
            @SuppressWarnings("rawtypes") List<Schema> schemas, String propertyName,
            AssociationType associationType, Names names) {
        List<String> otherClassNames = addAnonymousClassesAndReturnOtherClassNames(classes,
                relationships, name, schemas, names, propertyName);
        Inheritance inheritance = new Inheritance(name, otherClassNames, associationType,
                Optional.of(propertyName));
        relationships.add(inheritance);
    }

    private static void addInheritance(List<Class> classes, List<Relationship> relationships,
            String name, @SuppressWarnings("rawtypes") List<Schema> schemas, Names names) {
        List<String> otherClassNames = addAnonymousClassesAndReturnOtherClassNames(classes,
                relationships, name, schemas, names, null);
        relationships
                .add(new Inheritance(name, otherClassNames, AssociationType.ONE, Optional.empty()));
    }

    private static List<String> addAnonymousClassesAndReturnOtherClassNames(List<Class> classes,
            List<Relationship> relationships, String name,
            @SuppressWarnings("rawtypes") List<Schema> schemas, Names names, String property) {
        List<String> otherClassNames = schemas.stream() //
                .map(s -> {
                    if (s.get$ref() != null) {
                        return names.refToClassName(s.get$ref());
                    } else {
                        String className = names
                                .nextClassName(name + (property == null ? "" : "." + property));
                        Model m = toModelClass(className, s, names, ClassType.SCHEMA);
                        classes.addAll(m.classes());
                        relationships.addAll(m.relationships());
                        return className;
                    }
                }).collect(Collectors.toList());
        return otherClassNames;
    }

    private static void addToMany(List<Relationship> relationships, String name,
            String otherClassName) {
        addToMany(relationships, name, otherClassName, null);
    }

    private static void addToMany(List<Relationship> relationships, String name,
            String otherClassName, String property) {
        relationships.add(Association.from(name).to(otherClassName).many()
                .propertyOrParameterName(Optional.ofNullable(property)).build());
    }

    private static void addToOne(List<Relationship> relationships, String name,
            String otherClassName, String property, boolean isToOne) {
        relationships.add(Association //
                .from(name) //
                .to(otherClassName) //
                .type(isToOne ? AssociationType.ONE : AssociationType.ZERO_ONE) //
                .propertyOrParameterName(//
                        property == null || property.equals(otherClassName) ? Optional.empty()
                                : Optional.of(property))
                .build());
    }

    static Optional<String> getUmlTypeName(Schema<?> schema, Names names) {
        return getUmlTypeName(schema.get$ref(), schema, names);
    }

    static Optional<String> getUmlTypeName(String ref, Schema<?> schema, Names names) {
        final String type;
        if (ref != null) {
            type = names.refToClassName(ref);
        } else if (schema == null) {
            type = null;
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
            type = getUmlTypeName(a.getItems(), names) //
                    .orElseThrow(RuntimeException::new) + "[]";
        } else if (schema instanceof BinarySchema) {
            type = "byte[]";
        } else if (schema instanceof ByteArraySchema) {
            type = "byte[]";
        } else if (schema instanceof ObjectSchema) {
            type = "object";
        } else if (schema instanceof FileSchema) {
            type = "string";
        } else if (schema instanceof PasswordSchema) {
            type = "string";
        } else if (schema instanceof EmailSchema) {
            type = "string";
        } else if (schema instanceof UUIDSchema) {
            type = "string";
        } else if (schema instanceof MapSchema) {
            // TODO handle MapSchema
            type = "string";
        } else if (schema instanceof ComposedSchema) {
            // TODO handle ComposedSchema
            type = "string";
        } else if ("string".equals(schema.getType())) {
            type = "string";
        } else if (schema.get$ref() != null) {
            type = names.refToClassName(schema.get$ref());
        } else if (schema.getType() == null) {
            type = null;
        } else {
            throw new RuntimeException("not expected" + schema);
        }
        return Optional.ofNullable(type);
    }

}
