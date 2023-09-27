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
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DemoBatchTest {

    private static final File INPUTS = new File("src/test/resources/demo/");

    private final File input;

    public DemoBatchTest(File input) {
        this.input = input;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<?> files() {
        File[] list = INPUTS.listFiles();
        if (list == null) {
            return Collections.emptyList();
        } else {
            List<File> x = Arrays.asList(list);
            Collections.sort(x, (a, b) -> a.getName().compareTo(b.getName()));
            return x;
        }
    }

    @Test
    public void testBatch() {
        if (!"true".equalsIgnoreCase(System.getProperty("demo", "true"))) {
            return;
        }
        System.out.println("checking " + input);
        try (InputStream in = new FileInputStream(input)) {
            File demos = new File("target/demos");
            demos.mkdirs();
            File svg = new File(demos, input.getName().substring(0, input.getName().lastIndexOf('.')) + ".svg");
            String puml;
            try (InputStream def = new FileInputStream(input)) {
                puml = com.github.davidmoten.oas3.puml.Converter.openApiToPuml(def, true);
            }
            File pumlFile = new File(demos, input.getName().substring(0, input.getName().lastIndexOf('.')) + ".puml");
            pumlFile.delete();
            Files.write(pumlFile.toPath(), puml.getBytes(StandardCharsets.UTF_8));
            svg.delete();
            ConverterTest.writeSvg(input, svg.getPath(), true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
