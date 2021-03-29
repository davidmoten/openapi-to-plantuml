package com.github.davidmoten.oas3.puml2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.oas3.model.ClassType;
import com.github.davidmoten.oas3.model.Model;
import com.github.davidmoten.oas3.model.Relationship;
import com.github.davidmoten.oas3.puml.Names;
import com.github.davidmoten.oas3.puml.Util;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class Converter {

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
                + "\nhide " + toStereotype(ClassType.METHOD) + " circle" //
                + "\nhide empty methods" //
                + "\nhide empty fields" //
                + "\nset namespaceSeparator none" //
                + toPlantUml(model) //
                + "\n\n@enduml";
    }

    private static String toPlantUml(Model model) {
        StringBuilder b = new StringBuilder();
        for (com.github.davidmoten.oas3.model.Class cls : model.classes()) {
            b.append("\n\nclass " + Util.quote(cls.name())
                    + toStereotype(cls.type()).map(x -> " <<" + x + ">>").orElse("") + " {");
            cls.fields().stream().forEach(f -> {
                b.append("\n  " + f.name() + " : " + f.type());
            });
            b.append("\n}");
        }
        for (Relationship r : model.relationships()) {

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
