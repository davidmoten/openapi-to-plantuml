package com.github.davidmoten.oas3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.oas3.internal.model.Association;
import com.github.davidmoten.oas3.internal.model.Class;
import com.github.davidmoten.oas3.internal.model.ClassType;
import com.github.davidmoten.oas3.internal.model.Model;

public class ModelTest {

    @Test
    public void testToString() {
        Model model = new Model(//
                Lists.newArrayList( //
                        new Class("Thing", ClassType.SCHEMA), //
                        new Class("Stuff", ClassType.SCHEMA)),
                Lists.newArrayList( //
                        Association.from("Thing").to("Stuff").zeroOne().build(), //
                        Association.from("Thing").to("Other").one().build()));
        assertEquals("Model [\n" + "  Class [name=Thing, type=Schema, fields=[]],\n"
                + "  Class [name=Stuff, type=Schema, fields=[]]\n"
                + "  Association [from=Thing, to=Stuff, type=ZERO_ONE, responseCode=, responseContentType=, propertyOrParameterName=],\n"
                + "  Association [from=Thing, to=Other, type=ONE, responseCode=, responseContentType=, propertyOrParameterName=]\n"
                + "]", model.toString());
    }

}
