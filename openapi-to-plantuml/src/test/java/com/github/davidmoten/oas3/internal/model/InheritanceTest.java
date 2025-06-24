package com.github.davidmoten.oas3.internal.model;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;

public class InheritanceTest {

    @Test
    public void testToString() {
        Inheritance a = new Inheritance("Vehicle", Lists.newArrayList("Car", "Motorbike"), AssociationType.ONE,
                Optional.of("vehicle"));
        assertEquals("Inheritance [from=Vehicle, to=[Car, Motorbike], type=ONE, propertyName=Optional[vehicle]]",
                a.toString());
        assertEquals(AssociationType.ONE, a.type());
    }

}
