package com.github.davidmoten.openapitopuml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.Test;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class PumlTest {

    @Test
    public void testConvert() {
        String openapi = "openapi: 3.0.1\n" + "components:\n" + "  schemas:\n" + "    CustomerType:\n"
                + "      type: string\n" + "      example: Example value\n" + "    Customer:\n" + "      properties:\n"
                + "        firstName:\n" + "          type: string\n" + "        lastName:\n"
                + "          type: string\n" + "        heightMetres:\n" + "          type: number\n"
                + "        type:\n" + "          $ref: '#/components/schemas/CustomerType'\n" + "        friends:\n"
                + "          type: array\n" + "          items:\n"
                + "            $ref: '#/components/schemas/Customer'\n" + "      ";

        Puml.openApiToPuml(openapi);
    }

    @Test
    public void testConvertCts() throws IOException {
        try (InputStream in = PumlTest.class.getResourceAsStream("/openapi-1.yml")) {
            String puml = Puml.openApiToPuml(in);
            System.out.println(puml);
            SourceStringReader reader = new SourceStringReader(puml);
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                // Write the first image to "os"
                reader.generateImage(os, new FileFormatOption(FileFormat.SVG));

                File file = new File("src/docs/openapi-1.svg");
                file.delete();
                Files.write(file.toPath(), os.toByteArray());
            }
        }
    }
}
