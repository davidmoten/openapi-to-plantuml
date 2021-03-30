package com.github.davidmoten.oas3.puml2;

import static com.github.davidmoten.oas3.puml2.Util.quote;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.oas3.model.Association;
import com.github.davidmoten.oas3.model.AssociationType;
import com.github.davidmoten.oas3.model.Class;
import com.github.davidmoten.oas3.model.ClassType;
import com.github.davidmoten.oas3.model.Inheritance;
import com.github.davidmoten.oas3.model.Model;
import com.github.davidmoten.oas3.model.Relationship;
import com.github.davidmoten.oas3.puml.Names;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class Converter {

    private static final String SPACE = " ";

    public static String openApiToPuml(InputStream in) throws IOException {
        return openApiToPuml(IOUtils.toString(in, StandardCharsets.UTF_8));
    }

    public static String openApiToPuml(String openApi) {
        SwaggerParseResult result = new OpenAPIParser().readContents(openApi, null, null);

        // or from a file
        // SwaggerParseResult result = new
        // OpenAPIParser().readContents("./path/to/openapi.yaml" null, null);

        // the parsed POJO

        OpenAPI a = result.getOpenAPI();
        Names names = new Names(a);
        Model model = ComponentsHelper //
                .toModel(names) //
                .add(PathsHelper.toModel(names));
        return "@startuml" //
                + "\nhide <<" + toStereotype(ClassType.METHOD).get() + ">> circle" //
                + "\nhide empty methods" //
                + "\nhide empty fields" //
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
                b.append("\n  " + f.name() + " : " + f.type() + (f.isRequired() ? "" : " {O}"));
            });
            b.append("\n}");
        }

        for (Relationship r : model.relationships()) {
            if (r instanceof Association) {
                Association a = (Association) r;
                Class c = model //
                        .classes() //
                        .stream() //
                        .filter(x -> x.name().equals(a.from())) //
                        .findFirst() //
                        .orElseThrow(() -> new RuntimeException("could not find class " + a.from()));
                final String arrow;
                if (c.type() == ClassType.METHOD) {
                    arrow = "..>";
                } else {
                    arrow = "-->";
                }
                final String mult;
                if (a.type() == AssociationType.ONE) {
                    mult = "1";
                } else if (a.type() == AssociationType.ZERO_ONE) {
                    mult = "0..1";
                } else {
                    mult = "*";
                }

                b.append("\n\n" + quote(a.from()) + SPACE + arrow + SPACE + quote(mult) + SPACE + quote(a.to())
                        + a.label().map(x -> " : " + quote(x)).orElse(""));
            } else {
                Inheritance a = (Inheritance) r;
                anonNumber++;
                String diamond = "anon" + anonNumber;
                b.append("\n\ndiamond " + diamond);
                b.append("\n\n" + quote(a.from()) + SPACE + "-->" + SPACE + quote(diamond) + a.label().map(x -> " : " + x).orElse(""));
                for (String otherClassName : a.to()) {
                    b.append("\n\n" + quote(otherClassName) + SPACE + "--|>"  + SPACE + quote(diamond));
                }
            }
        }
        return b.toString();
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
