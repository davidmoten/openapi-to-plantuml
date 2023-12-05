package com.github.davidmoten.oas3.puml;

import static com.github.davidmoten.oas3.internal.Util.quote;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.oas3.internal.ComponentsHelper;
import com.github.davidmoten.oas3.internal.Names;
import com.github.davidmoten.oas3.internal.PathsHelper;
import com.github.davidmoten.oas3.internal.Util;
import com.github.davidmoten.oas3.internal.model.Association;
import com.github.davidmoten.oas3.internal.model.AssociationType;
import com.github.davidmoten.oas3.internal.model.Class;
import com.github.davidmoten.oas3.internal.model.ClassType;
import com.github.davidmoten.oas3.internal.model.Field;
import com.github.davidmoten.oas3.internal.model.HasPuml;
import com.github.davidmoten.oas3.internal.model.Inheritance;
import com.github.davidmoten.oas3.internal.model.Model;
import com.github.davidmoten.oas3.internal.model.ModelTransformer;
import com.github.davidmoten.oas3.internal.model.ModelTransformerExtract;
import com.github.davidmoten.oas3.internal.model.PumlExtract;
import com.github.davidmoten.oas3.internal.model.Relationship;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Converter {

    private static final String COLON = " : ";
    private static final String SPACE = " ";

    private Converter() {
        // prevent instantiation
    }

    public static String openApiToPuml(InputStream in) throws IOException {
        return openApiToPuml(in, ModelTransformer.identity()).puml();
    }

    public static List<PumlExtract> openApiToPumlSplitByMethod(File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return openApiToPumlSplitByMethod(in);
        }
    }

    public static List<PumlExtract> openApiToPumlSplitByMethod(InputStream in) throws IOException {
        OpenAPI api = parseOpenApi(IOUtils.toString(in, StandardCharsets.UTF_8));
        Model m = toModel(api);
        return m //
                .classes() //
                .stream() //
                .filter(c -> c.type() == ClassType.METHOD) //
                .map(c -> {
                    ModelTransformerExtract t = new ModelTransformerExtract(Collections.singleton(c.name()), false);
                    Model model = t.apply(m);
                    return t.createHasPuml(toPlantUml(model));
                }) //
                .collect(Collectors.toList());
    }

    public static <T extends HasPuml> T openApiToPuml(InputStream in, ModelTransformer<T> transformer)
            throws IOException {
        return openApiToPuml(IOUtils.toString(in, StandardCharsets.UTF_8), transformer);
    }

    public static String openApiToPuml(File file) throws IOException {
        return openApiToPuml(file, ModelTransformer.identity()).puml();
    }

    public static <T extends HasPuml> T openApiToPuml(File file, ModelTransformer<T> transformer) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return openApiToPuml(in, transformer);
        }
    }

    public static <T extends HasPuml> T openApiToPuml(String openApi, ModelTransformer<T> transformer) {
        return openApiToPuml(parseOpenApi(openApi), transformer);
    }

    private static OpenAPI parseOpenApi(String openApi) {
        System.setProperty("maxYamlCodePoints", "999999999");
        SwaggerParseResult result = new OpenAPIParser().readContents(openApi, null, null);
        if (result.getOpenAPI() == null) {
            throw new IllegalArgumentException("Not an OpenAPI definition");
        }
        return result.getOpenAPI();
    }

    public static String openApiToPuml(String openApi) {
        return openApiToPuml(openApi, ModelTransformer.identity()).puml();
    }

    private static <T extends HasPuml> T openApiToPuml(OpenAPI a, ModelTransformer<T> transformer) {
        Model m = toModel(a);
        Model model = transformer.apply(m);
        return transformer.createHasPuml(toPlantUml(model));
    }

    private static Model toModel(OpenAPI a) {
        Names names = new Names(a);
        return ComponentsHelper //
                .toModel(names) //
                .add(PathsHelper.toModel(names));
    }

    private static String toPlantUml(Model model) {
        return "@startuml" //
                + "\nhide <<" + toStereotype(ClassType.METHOD).get() + ">> circle" //
                + "\nhide <<" + toStereotype(ClassType.RESPONSE).get() + ">> circle" //
                + "\nhide <<" + toStereotype(ClassType.PARAMETER).get() + ">> circle" //
                + "\nhide empty methods" //
                + "\nhide empty fields" //
                + "\nskinparam class {"
                + "\nBackgroundColor<<Path>> Wheat"
                + "\n}"
                // make sure that periods in class names aren't interpreted as namespace
                // separators (which results in recursive boxing)
                + "\nset namespaceSeparator none" //
                + toPlantUmlInner(model) //
                + "\n\n@enduml";
    }

    private static String toPlantUmlInner(Model model) {
////        model = new ModelConverterLinksThreshold(10).apply(model);
//        model = new ModelConverterExtract(Collections.singleton("GET.*athletes.*routes"), true).apply(model);
        int anonNumber = 0;
        StringBuilder b = new StringBuilder();
        for (Class cls : model.classes()) {
            if (cls.isEnum()) {
                b.append("\n\nenum " + Util.quote(cls.name())
                        + toStereotype(cls.type()).map(x -> " <<" + x + ">>").orElse("") + " {");
                int max = Integer.getInteger("max.enum.entries", 12);
                if (max == 0) {
                    max = Integer.MAX_VALUE;
                }
                List<Field> fields = cls.fields().subList(0, Math.min(max, cls.fields().size()));
                fields.stream().forEach(f -> {
                    b.append("\n  " + f.name());
                });
                if (cls.fields().size() > max) {
                    b.append("\n ...");
                }
                b.append("\n}");
            } else {
                b.append("\n\nclass " + Util.quote(cls.name())
                        + toStereotype(cls.type()).map(x -> " <<" + x + ">> ").orElse("")
                        + cls.description().map(x -> " <<" + x + ">> ").orElse("")
                        + " {");
                cls.fields().stream().forEach(f -> {
                    b.append("\n  {field} " + f.name() + COLON + f.type() + (f.isRequired() ? "" : " {O}"));
                });
                b.append("\n}");
//                cls.description().ifPresent(desc -> {
//                    b.append("\nnote top of " + Util.quote(cls.name()) + ": " + desc);
//                });
            }
        }
        // add external ref classes
        Set<String> added = new HashSet<>();
        for (Relationship r : model.relationships()) {
            final String to;
            if (r instanceof Association) {
                Association a = (Association) r;
                to = a.to();
            } else {
                Inheritance a = (Inheritance) r;
                to = a.from();
            }
            if (!added.contains(to) && to.contains(Names.NAMESPACE_DELIMITER)) {
                String[] items = to.split(Names.NAMESPACE_DELIMITER);
                String namespace = items[0];
                String clsName = items[1];
                b.append("\n\nclass " + Util.quote(clsName) + " <<" + namespace + ">>" + " {");
                b.append("\n}");
                added.add(to);
            }
        }

        for (Relationship r : model.relationships()) {
            if (r instanceof Association) {
                Association a = (Association) r;

                final String mult = toMultiplicity(a.type());

                final String label;
                final String arrow;
                if (a.responseCode().isPresent()) {
                    arrow = (a.owns() ? "*" : "") + "..>";
                    label = a.responseCode().get() + a.responseContentType()
                            .filter(x -> !"application/json".equalsIgnoreCase(x)).map(x -> SPACE + x).orElse("");
                } else {
                    arrow = (a.owns() ? "*" : "") + "-->";
                    label = a.propertyOrParameterName().orElse("");
                }
                String to = a.to();
                if (to.contains(Names.NAMESPACE_DELIMITER)) {
                    to = to.split(Names.NAMESPACE_DELIMITER)[1];
                }
                b.append("\n\n" + quote(a.from()) + SPACE + arrow + SPACE + quote(mult) + SPACE + quote(to)
                        + (label.equals("") ? "" : SPACE + COLON + SPACE + quote(label)));
            } else {
                Inheritance a = (Inheritance) r;
                String from = a.from();
                if (from.contains(Names.NAMESPACE_DELIMITER)) {
                    from = from.split(Names.NAMESPACE_DELIMITER)[1];
                }
                if (a.propertyName().isPresent() || a.type() != AssociationType.ONE) {
                    String mult = toMultiplicity(a.type());
                    anonNumber++;
                    String diamond = "anon" + anonNumber;
                    b.append("\n\ndiamond " + diamond);
                    b.append("\n\n" + quote(from) + SPACE + "-->" + quote(mult) + SPACE + quote(diamond)
                            + a.propertyName().map(x -> COLON + quote(x)).orElse(""));
                    for (String otherClassName : a.to()) {
                        b.append("\n\n" + quote(otherClassName) + SPACE + "--|>" + SPACE + quote(diamond));
                    }
                } else {
                    for (String otherClassName : a.to()) {
                        b.append("\n\n" + quote(otherClassName) + SPACE + "--|>" + SPACE + quote(a.from()));
                    }
                }
            }
        }
        return b.toString();
    }

    private static String toMultiplicity(AssociationType type) {
        final String mult;
        if (type == AssociationType.ONE) {
            mult = "1";
        } else if (type == AssociationType.ZERO_ONE) {
            mult = "0..1";            
        } else if (type == AssociationType.ONE_MANY) {
          mult = "0..*"; 
        }
        else {
            mult = "*";
        }
        return mult;
    }

    private static Optional<String> toStereotype(ClassType type) {
        final String result;
        if (type == ClassType.METHOD) {
            result = "Path";
        } else if (type == ClassType.PARAMETER) {
            result = "Parameter";
        } else if (type == ClassType.REQUEST_BODY) {
            result = "RequestBody";
        } else if (type == ClassType.RESPONSE) {
            result = "Response";
        } else {
            result = null;
        }
        return Optional.ofNullable(result);
    }

}
