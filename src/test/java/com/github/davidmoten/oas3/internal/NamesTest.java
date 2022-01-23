package com.github.davidmoten.oas3.internal;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Test;

public class NamesTest {

	private static Names emptyNames() {
		OpenAPI a = new OpenAPI();
		return new Names(a);
	}

	@Test(expected = RuntimeException.class)
	public void testRefToClassNameNotFound() {
		emptyNames().refToClassName("abc");
	}
}
