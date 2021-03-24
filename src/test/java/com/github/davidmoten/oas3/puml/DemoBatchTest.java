package com.github.davidmoten.oas3.puml;

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
public class DemoBatchTest {

    private static final File inputs = new File("src/test/resources/demo/");

    private final File input;

    public DemoBatchTest(File input) {
        this.input = input;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<?> files() {
        File[] list = inputs.listFiles();
        if (list == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(list);
        }
    }

    @Test
    public void testBatch() {
        System.out.println("checking " + input);
        try (InputStream in = new FileInputStream(input)) {
            File demos = new File("target/demos");
            demos.mkdirs();
            File svg = new File(demos, input.getName().substring(0, input.getName().lastIndexOf('.')) + ".svg");
            String puml;
            try (InputStream def = new FileInputStream(input)) {
                puml = Converter.openApiToPuml(def);
            }
            File pumlFile = new File(demos, input.getName().substring(0, input.getName().lastIndexOf('.')) + ".puml");
            pumlFile.delete();
            Files.write(pumlFile.toPath(), puml.getBytes(StandardCharsets.UTF_8));
            svg.delete();
            ConverterTest.writeSvg(input, svg.getPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
