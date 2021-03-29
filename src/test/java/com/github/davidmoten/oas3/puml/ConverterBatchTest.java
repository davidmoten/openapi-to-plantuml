package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.github.davidmoten.oas3.puml.Converter;

@RunWith(Parameterized.class)
public class ConverterBatchTest {

    private static final File inputs = new File("src/test/resources/inputs/");
    private static final File outputs = new File("src/test/resources/outputs/");

    private final File input;

    public ConverterBatchTest(File input) {
        this.input = input;
    }

    @Parameterized.Parameters(name="{0}")
    public static Collection<?> files() {
        File[] list = inputs.listFiles();
        if (list == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(list);
        }
    }

    @Test
    public void test() {
        System.out.println("checking " + input);
        try (InputStream in = new FileInputStream(input)) {
            String puml = com.github.davidmoten.oas3.puml2.Converter.openApiToPuml(in).trim();
            File pumlFile = new File("target/outputs", input.getName().substring(0, input.getName().lastIndexOf('.')) + ".puml");
            pumlFile.getParentFile().mkdirs();
            pumlFile.delete();
            Files.write(pumlFile.toPath(), puml.getBytes(StandardCharsets.UTF_8));
            File output = new File(outputs, input.getName().substring(0, input.getName().lastIndexOf('.')) + ".puml");
            if (!output.exists()) {
                output.createNewFile();
                System.out.println(puml);
            }
            String expected = new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8).trim();
            ConverterTest.writeSvg(input, "target/outputs/" + output.getName() + ".svg");
            assertEquals(expected, puml);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
