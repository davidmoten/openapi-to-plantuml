package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.Test;

import com.github.davidmoten.junit.Files;

import net.sourceforge.plantuml.code.TranscoderSmart;

public class DeflaterTest {

    @Test
    public void test() throws DataFormatException, IOException {
        String encodedUml = Files.readUtf8(new File("src/test/resources/encodedUml.txt"));
        assertTrue(new TranscoderSmart().decode(encodedUml).startsWith("@startuml"));
        // System.out.println("https://planttext.com/api/plantuml/img/" + encodedUml);
    }

    @Test
    public void testEncode() throws IOException {
        String uml = "@startuml\nBob->Alice: hello\n@enduml";
        String path = new TranscoderSmart().encode(uml);
        System.out.println("https://planttext.com/api/plantuml/img/" + path);
    }

}
