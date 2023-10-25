package com.github.davidmoten.oas3.puml;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

import net.sourceforge.plantuml.FileFormat;

public class ConverterMainTest {

    @Test
    public void testToImages() throws IOException {
        for (FileFormat ff : new FileFormat[] {FileFormat.PNG, FileFormat.SVG, FileFormat.LATEX}) {
            try {
                String[] args = new String[] {"src/test/resources/openapi-example.yml",
                        ff.toString(),
                        new File("target/converted." + ff.getFileSuffix()).getPath()};
                ConverterMain.main(args);
            } catch (Throwable e) {
                //
            }
        }
    }

    @Test
    public void testToPuml() throws IOException {
        String[] args = new String[] {"single", "src/test/resources/openapi-example.yml", "PUML",
                "target/converted.puml"};
        ConverterMain.main(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNumberOfArgs() throws IOException {
        ConverterMain.main(new String[] {"a"});
    }

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(ConverterMain.class);
    }

}
