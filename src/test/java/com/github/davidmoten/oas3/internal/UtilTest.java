package com.github.davidmoten.oas3.internal;

import com.github.davidmoten.junit.Asserts;
import org.junit.Test;

public class UtilTest {

	@Test
	public void testIsUtilityClass() {
		Asserts.assertIsUtilityClass(Util.class);
	}

}
