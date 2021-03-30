package com.github.davidmoten.oas3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.oas3.internal.model.Association;

public class AssociationTest {

    @Test
    public void testToString() {
        Association a = Association.from("thing").to("stuff").zeroOne().propertyOrParameterName("hello")
                .responseCode("200").responseContentType("application/json").build();
        assertEquals(
                "Association [from=thing, to=stuff, type=ZERO_ONE, responseCode=200, responseContentType=application/json, propertyOrParameterName=hello]",
                a.toString());
    }

}
