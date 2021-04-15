package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.DataFormatException;

import org.junit.Test;

import net.sourceforge.plantuml.code.TranscoderSmart2;

public class DeflaterTest {

    @Test
    public void test() throws DataFormatException, IOException {
        String encodedUml = Files.readString(new File("src/test/resources/encodedUml.txt").toPath(),
                StandardCharsets.UTF_8);
        assertTrue(new TranscoderSmart2().decode(encodedUml).startsWith("@startuml"));
        // System.out.println("https://planttext.com/api/plantuml/img/" + encodedUml);
    }

}
