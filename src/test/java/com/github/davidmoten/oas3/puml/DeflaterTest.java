package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;

import org.junit.Test;

import net.sourceforge.plantuml.code.NoPlantumlCompressionException;
import net.sourceforge.plantuml.code.TranscoderSmart2;

public class DeflaterTest {

    @Test
    public void test() throws DataFormatException, UnsupportedEncodingException,
            NoPlantumlCompressionException {
        String encodedUml = "XP1DYy8m48Rlyok6dbQN7FOg7YfwKF2q29v3EzXXavdIfCgY_dTRsog27xUyvCdpJYODqLQnzWuWSdI4l-HiP9LGS1dGuDpP4731TbTP3m1Pbm_a7CiEZu3ulPA8MvPS3w6DU-KSrvhzRGfQg5PV8pWF3sTbK-T9Of-NMWVgptFrlfOXTSAXhz40t5gd9zFSYRdh9hYIWYgELZ9w0lRkJzXrd1TGyfFWsDIbmSHR-K_w2IUjkzJ0xTRUqEqN7bb8IV9czHS0";
        assertTrue(new TranscoderSmart2().decode(encodedUml).startsWith("@startuml"));
        //System.out.println("https://planttext.com/api/plantuml/img/" + encodedUml);
    }

}
