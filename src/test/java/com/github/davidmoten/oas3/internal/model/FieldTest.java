package com.github.davidmoten.oas3.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.davidmoten.oas3.internal.model.Field;

public class FieldTest {

    @Test
    public void testToString() {
        Field f = new Field("aliases", "string[]", true, false);
        assertEquals("Field [name=aliases, type=string[], isArray=true, required=false]", f.toString());
        assertTrue(f.isArray());
    }

}
