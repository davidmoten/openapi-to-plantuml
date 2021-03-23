package com.github.davidmoten.oa2puml.v3;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Ignore;
import org.junit.Test;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

public class ConverterTest {

    private static final File OPENAPI_EXAMPLE = new File("src/test/resources/openapi-example.yml");

    @Test
    public void testConvert() {
        String openapi = "openapi: 3.0.1\n" + "components:\n" + "  schemas:\n" + "    CustomerType:\n"
                + "      type: string\n" + "      example: Example value\n" + "    Customer:\n" + "      properties:\n"
                + "        firstName:\n" + "          type: string\n" + "        lastName:\n"
                + "          type: string\n" + "        heightMetres:\n" + "          type: number\n"
                + "        type:\n" + "          $ref: '#/components/schemas/CustomerType'\n" + "        friends:\n"
                + "          type: array\n" + "          items:\n"
                + "            $ref: '#/components/schemas/Customer'\n" + "      ";

        Converter.openApiToPuml(openapi);
    }

    @Test
    public void testConvertPumlToSvg() throws IOException {
        writeSvg(OPENAPI_EXAMPLE, "target/openapi-example.svg");
    }

    @Test
    @Ignore
    public void updateDocs() throws IOException {
        writeSvg(OPENAPI_EXAMPLE,"src/docs/openapi-example.svg");
    }
    
    @Test
    public void testReadString() throws IOException {
        assertFalse(readString("src/test/resources/openapi-example.yml").isEmpty());
    }

    private static String readString(String filename) throws IOException {
        return new String(Files.readAllBytes(new File(filename).toPath() ), StandardCharsets.UTF_8);
    }
    
    static void writeSvg(File openApiFile, String filename) throws IOException {
        try (InputStream in = new FileInputStream(openApiFile)) {
            String puml = Converter.openApiToPuml(in);
            SourceStringReader reader = new SourceStringReader(puml);
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                // Write the first image to "os"
                DiagramDescription result = reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
                System.out.println("  svg result: " + result.getDescription());

                File file = new File(filename);
                file.delete();
                Files.write(file.toPath(), os.toByteArray());
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        writeSvg(new File(System.getProperty("user.home","") +  "/github.yml"), "target/github.yml.svg");
    }
}
