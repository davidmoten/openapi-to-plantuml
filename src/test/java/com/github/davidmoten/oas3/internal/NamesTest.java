package com.github.davidmoten.oas3.internal;

import java.util.Optional;

import org.junit.Test;

import io.swagger.v3.oas.models.OpenAPI;

public class NamesTest {

    @Test
    public void testRefToClassNameNotFound() {
        emptyNames().refToClassName("abc");
    }

    private static Names emptyNames() {
        OpenAPI a = new OpenAPI();
        return new Names(Optional.empty(), a);
    }
}
