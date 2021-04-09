package com.github.davidmoten.oas3.puml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class ConverterMain {

    private ConverterMain() {
        // prevent instantiation
    }

    public static void main(String[] args) throws IOException {
        String usage = "Usage: java -jar openapi-to-plantuml-all.jar <OPENAPI_FILE> (SVG|JPG|PUML) <OUTPUT_FILE>";
        if (args.length != 3) {
            System.out.println(usage);
            System.exit(1);
        } else {
            String puml = Converter.openApiToPuml(new File(args[0]));
            String format = args[1];
            File out = new File(args[2]);
            if (format.equals("PUML")) {
                Files.write(out.toPath(), puml.getBytes(StandardCharsets.UTF_8));
            } else {
                // TODO
            }
        }
    }

}
