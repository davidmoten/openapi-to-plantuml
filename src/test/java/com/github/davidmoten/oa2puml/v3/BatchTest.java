package com.github.davidmoten.oa2puml.v3;

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

@RunWith(Parameterized.class)
public class BatchTest {

    private static final File inputs = new File("src/test/resources/inputs/");
    private static final File outputs = new File("src/test/resources/outputs/");

    private final File input;

    public BatchTest(File input) {
        this.input = input;
    }

    @Parameterized.Parameters
    public static Collection<?> primeNumbers() {
        File[] list = inputs.listFiles();
        if (list != null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(list);
        }
    }

    @Test
    public void testBatch() {
        if (true) throw new RuntimeException(input.toString());
        try (InputStream in = new FileInputStream(input)) {
            String puml = Converter.openApiToPuml(in).trim();
            File output = new File(outputs, input.getName().substring(0, input.getName().lastIndexOf('.')) + ".puml");
            if (!output.exists()) {
                output.createNewFile();
                System.out.println(puml);
            }
            System.out.println("checking " + input);
            String expected = new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8).trim();
            assertEquals(expected, puml);
            System.out.println(input + " passed");
            ConverterTest.writeSvg(input, "target/" + output.getName() + ".svg");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
