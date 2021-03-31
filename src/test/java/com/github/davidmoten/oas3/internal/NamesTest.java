package com.github.davidmoten.oas3.internal;

import org.junit.Test;

import io.swagger.v3.oas.models.OpenAPI;

public class NamesTest {

    @Test(expected = RuntimeException.class)
    public void testRefToClassNameNotFound() {
        emptyNames().refToClassName("abc");
    }

    private static Names emptyNames() {
        OpenAPI a = new OpenAPI();
        return new Names(a);
    }
}
