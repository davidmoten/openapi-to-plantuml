package com.github.davidmoten.openapitopuml;

import org.junit.Test;

public class PumlTest {
    
    @Test
    public void testConvert() {
        String openapi = "openapi: 3.0.1\n" + 
                "servers:\n" + 
                "- url: /\n" + 
                "paths:\n" + 
                "  /newPerson:\n" + 
                "    post:\n" + 
                "      summary: Create new person\n" + 
                "      description: Create new person\n" + 
                "      responses:\n" + 
                "        200:\n" + 
                "          description: ok\n" + 
                "          content:\n" + 
                "            '*/*':\n" + 
                "              schema:\n" + 
                "                type: string\n" + 
                "                example: Example value\n" + 
                "components:\n" + 
                "  schemas:\n" + 
                "    CustomerType:\n" + 
                "      type: string\n" + 
                "      example: Example value";
        System.out.println(Puml.toPuml(openapi));
    }

}
