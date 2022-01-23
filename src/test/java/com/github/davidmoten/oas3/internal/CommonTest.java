package com.github.davidmoten.oas3.internal;

import com.github.davidmoten.junit.Asserts;
import org.junit.Test;

public class CommonTest {

	@Test
	public void testIsUtilityClass() {
		Asserts.assertIsUtilityClass(Common.class);
	}

}
