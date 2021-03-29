package com.github.davidmoten.oas3.puml2;

import static com.github.davidmoten.oas3.puml.Constants.CLASS_RELATIONSHIP_RIGHT_ARROW;
import static com.github.davidmoten.oas3.puml.Constants.NL;
import static com.github.davidmoten.oas3.puml.Constants.ONE;
import static com.github.davidmoten.oas3.puml.Constants.PATH_RELATIONSHIP_RIGHT_ARROW;
import static com.github.davidmoten.oas3.puml.Constants.SPACE;
import static com.github.davidmoten.oas3.puml.Util.first;
import static com.github.davidmoten.oas3.puml.Util.nullListToEmpty;
import static com.github.davidmoten.oas3.puml.Util.quote;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.oas3.model.Model;
import com.github.davidmoten.oas3.puml.Names;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

public final class PathsHelper {

    public static String toPlantUml(Names names) {
        return paths(names);
    }
    
    private static String paths(Names names) {
        if (names.paths() == null) {
            return "";
        } else {
            return names.paths() //
                    .entrySet() //
                    .stream() //
                    .map(entry -> toPlantUmlPath(entry.getKey(), //
                            entry.getValue(), names))
                    .collect(joining());
        }
    }


    private static String toPlantUmlPath(String path, PathItem p, Names names) {
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
                    s.append(toPlantUmlParameters(names, extras, className, operation.getParameters()));
                    s.append("\n}");
                    s.append(toPlantUmlResponses(names, operation, className));
                    s.append(toPlantUmlRequestBody(className, operation, names));
                    return s.toString();
                }) //
                .collect(joining()));
        b.append(extras.toString());
        return b.toString();
    }

    private static String toPlantUmlParameters(Names names, StringBuilder extras, String className,
            List<Parameter> parameters) {
        return nullListToEmpty(parameters) //
                .stream()//
                .map(param -> toPlantUmlParameter(names, extras, className, param)) //
                .collect(joining());
    }

    private static String toPlantUmlParameter(Names names, StringBuilder extras, String className, Parameter param) {
        String ref = param.get$ref();
        String parameterName = param.getName();
        Boolean required = param.getRequired();
        if (ref != null) {
            // resolve the parameter ref chain so we can get the parameter `name` and
            // `required` fields
            Parameter p = param;
            while (p.get$ref() != null) {
                String r = p.get$ref();
                p = getParameter(names.components(), r);
            }
            // override name with name from last ref
            parameterName = p.getName();
            required = p.getRequired();
        }

        if (ref != null) {
            extras.append("\n\n" + quote(className) + CLASS_RELATIONSHIP_RIGHT_ARROW + quote("1") + SPACE
                    + quote(names.refToClassName(ref)) + " : " + quote(parameterName));
            return "";
        }
        if (param.getSchema() != null) {
            Common.toPlantUmlClass(className + "." + parameterName, param.getSchema(), names, Stereotype.PARAMETER);
        }
        // TODO else get schema from content
        final String type = Common.getUmlTypeName(param.get$ref(), param.getSchema(), names);
        if (Common.isSimpleType(type)) {
            final String optional = required != null && required ? "" : " {O}";
            return "\n" + "  " + parameterName + " : " + type + optional;
        } else {
            extras.append("\n\n" + quote(className) + CLASS_RELATIONSHIP_RIGHT_ARROW + quote("1") + SPACE + quote(type)
                    + " : " + quote(parameterName));
            return "";
        }
    }

    private static Parameter getParameter(Components components, String ref) {
        Preconditions.checkNotNull(ref);
        Reference r = new Reference(ref);
        if ("#/components/parameters".equals(r.namespace)) {
            return components.getParameters().get(r.simpleName);
        } else {
            throw new RuntimeException("unexpected");
        }
    }

    private static String toPlantUmlRequestBody(String className, Operation operation, Names names) {
        RequestBody body = operation.getRequestBody();
        if (body != null) {
            String ref = body.get$ref();
            if (ref != null) {
                return NL + quote(className) + CLASS_RELATIONSHIP_RIGHT_ARROW + ONE + quote(names.refToClassName(ref));
            }
            Content content = body.getContent();
            if (content != null) {
                Entry<String, MediaType> mediaType = first(content).get();
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
                        requestBodyClassDeclaration = Common.toPlantUmlClass(requestBodyClassName, sch, names,
                                Stereotype.REQUEST_BODY);
                    }
                }
                return requestBodyClassDeclaration + "\n\n" + quote(className) + CLASS_RELATIONSHIP_RIGHT_ARROW
                        + quote(requestBodyClassName);
            }
        }
        return "";
    }

    private static String toPlantUmlResponses(Names names, Operation operation, String className) {
        return operation //
                .getResponses() //
                .entrySet() //
                .stream() //
                .map(ent -> {
                    String responseCode = ent.getKey();
                    // TODO only using the first content
                    ApiResponse r = ent.getValue();
                    final String returnClassName;
                    final String returnClassDeclaration;
                    if (r.get$ref() != null) {
                        returnClassName = names.refToClassName(r.get$ref());
                        returnClassDeclaration = "";
                    } else {
                        final String newReturnClassName = className + " " + responseCode + " Response";
                        if (r.getContent() == null) {
                            returnClassDeclaration = "\nclass " + quote(newReturnClassName) + "{}";
                            returnClassName = newReturnClassName;
                        } else {
                            Optional<Entry<String, MediaType>> mediaType = first(r.getContent());
                            if (mediaType.isPresent()) {
                                Schema<?> sch = mediaType.get().getValue().getSchema();
                                if (sch != null && sch.get$ref() != null) {
                                    returnClassName = names.refToClassName(sch.get$ref());
                                    returnClassDeclaration = "";
                                } else {
                                    returnClassName = newReturnClassName;
                                    if (sch == null) {
                                        returnClassDeclaration = "";
                                    } else {
                                        returnClassDeclaration = Common.toPlantUmlClass(returnClassName, sch, names,
                                                Stereotype.RESPONSE);
                                    }
                                }
                            } else {
                                return "";
                            }
                        }
                    }
                    return returnClassDeclaration + "\n\n" + quote(className) + PATH_RELATIONSHIP_RIGHT_ARROW
                            + quote(returnClassName) + ": " + responseCode;
                }).collect(joining());
    }

    private static final class Reference {
        final String namespace;
        final String simpleName;

        Reference(String ref) {
            this.namespace = ref.substring(0, ref.lastIndexOf("/"));
            this.simpleName = ref.substring(ref.lastIndexOf("/") + 1);
        }
    }

    public static Model toModel(Names names) {
        // TODO Auto-generated method stub
        return null;
    }

}
