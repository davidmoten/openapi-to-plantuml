package com.github.davidmoten.oas3.internal;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class CommonTest {
    
    @Test
    public void testIsUtilityClass() {
        Asserts.assertIsUtilityClass(Common.class);
    }

}
