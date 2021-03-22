package com.github.davidmoten.oa2puml.v3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DemoBatchTest {

    private static final File inputs = new File("src/test/resources/demo/");

    private final File input;

    public DemoBatchTest(File input) {
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
    @Ignore
    public void testBatch() {
        System.out.println("checking " + input);
        try (InputStream in = new FileInputStream(input)) {
            File svg = new File("target", input.getName().substring(0, input.getName().lastIndexOf('.')) + ".svg");
            svg.delete();
            ConverterTest.writeSvg(input, svg.getPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
