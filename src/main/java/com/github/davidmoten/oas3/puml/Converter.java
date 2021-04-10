package com.github.davidmoten.oas3.puml;

import static com.github.davidmoten.oas3.internal.Util.quote;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.oas3.internal.ComponentsHelper;
import com.github.davidmoten.oas3.internal.Names;
import com.github.davidmoten.oas3.internal.PathsHelper;
import com.github.davidmoten.oas3.internal.Util;
import com.github.davidmoten.oas3.internal.model.Association;
import com.github.davidmoten.oas3.internal.model.AssociationType;
import com.github.davidmoten.oas3.internal.model.Class;
import com.github.davidmoten.oas3.internal.model.ClassType;
import com.github.davidmoten.oas3.internal.model.Inheritance;
import com.github.davidmoten.oas3.internal.model.Model;
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
        return openApiToPuml(IOUtils.toString(in, StandardCharsets.UTF_8));
    }

    public static String openApiToPuml(File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return openApiToPuml(in);
        }
    }

    public static String openApiToPuml(String openApi) {
        SwaggerParseResult result = new OpenAPIParser().readContents(openApi, null, null);
        return openApiToPuml(result.getOpenAPI());
    }

    public static String openApiToPuml(OpenAPI a) {

        Names names = new Names(a);
        Model model = ComponentsHelper //
                .toModel(names) //
                .add(PathsHelper.toModel(names));

        return "@startuml" //
                + "\nhide <<" + toStereotype(ClassType.METHOD).get() + ">> circle" //
                + "\nhide <<" + toStereotype(ClassType.RESPONSE).get() + ">> circle" //
                + "\nhide <<" + toStereotype(ClassType.PARAMETER).get() + ">> circle" //
                + "\nhide empty methods" //
                + "\nhide empty fields" //
                // make sure that periods in class names aren't interpreted as namespace
                // separators (which results in recursive boxing)
                + "\nset namespaceSeparator none" //
                + toPlantUml(model) //
                + "\n\n@enduml";
    }

    private static String toPlantUml(Model model) {
        int anonNumber = 0;
        StringBuilder b = new StringBuilder();
        for (Class cls : model.classes()) {
            b.append("\n\nclass " + Util.quote(cls.name())
                    + toStereotype(cls.type()).map(x -> " <<" + x + ">>").orElse("") + " {");
            cls.fields().stream().forEach(f -> {
                b.append("\n  " + f.name() + COLON + f.type() + (f.isRequired() ? "" : " {O}"));
            });
            b.append("\n}");
        }

        for (Relationship r : model.relationships()) {
            if (r instanceof Association) {
                Association a = (Association) r;

                final String mult = toMultiplicity(a.type());

                final String label;
                final String arrow;
                if (a.responseCode().isPresent()) {
                    arrow = "..>";
                    label = a.responseCode().get() + a.responseContentType()
                            .filter(x -> !"application/json".equalsIgnoreCase(x))
                            .map(x -> SPACE + x).orElse("");
                } else {
                    arrow = "-->";
                    label = a.propertyOrParameterName().orElse("");
                }
                b.append("\n\n" + quote(a.from()) + SPACE + arrow + SPACE + quote(mult) + SPACE
                        + quote(a.to())
                        + (label.equals("") ? "" : SPACE + COLON + SPACE + quote(label)));
            } else {
                Inheritance a = (Inheritance) r;
                if (a.label().isPresent() || a.type() != AssociationType.ONE) {
                    String mult = toMultiplicity(a.type());
                    anonNumber++;
                    String diamond = "anon" + anonNumber;
                    b.append("\n\ndiamond " + diamond);
                    b.append("\n\n" + quote(a.from()) + SPACE + "-->" + quote(mult) + SPACE
                            + quote(diamond) + a.label().map(x -> COLON + quote(x)).orElse(""));
                    for (String otherClassName : a.to()) {
                        b.append("\n\n" + quote(otherClassName) + SPACE + "--|>" + SPACE
                                + quote(diamond));
                    }
                } else {
                    for (String otherClassName : a.to()) {
                        b.append("\n\n" + quote(otherClassName) + SPACE + "--|>" + SPACE
                                + quote(a.from()));
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
        } else {
            mult = "*";
        }
        return mult;
    }

    private static Optional<String> toStereotype(ClassType type) {
        final String result;
        if (type == ClassType.METHOD) {
            result = "Method";
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
