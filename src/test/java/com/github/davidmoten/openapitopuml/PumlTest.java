package com.github.davidmoten.openapitopuml;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class PumlTest {

    @Test
    public void testConvert() {
        String openapi = "openapi: 3.0.1\n" + "components:\n" + "  schemas:\n" + "    CustomerType:\n"
                + "      type: string\n" + "      example: Example value\n" + "    Customer:\n" + "      properties:\n"
                + "        firstName:\n" + "          type: string\n" + "        lastName:\n"
                + "          type: string\n" + "        heightMetres:\n" + "          type: number\n"
                + "        type:\n" + "          $ref: '#/components/schemas/CustomerType'\n" + "        friends:\n"
                + "          type: array\n" + "          items:\n"
                + "            $ref: '#/components/schemas/Customer'\n" + "      ";

        //System.out.println(Puml.toPuml(openapi));
    }

    @Test
    public void testConvertCts() throws IOException {
        try (InputStream in = PumlTest.class.getResourceAsStream("/openapi-1.yml")) {
            System.out.println(Puml.toPuml(in));
        }
    }
}
