package com.github.davidmoten.oas3.internal.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Throwables
				extends Throwable {
	private final Set<Throwable> throwables = new LinkedHashSet<>();

	public Throwables() {
		super();
	}

	public Throwables(String message) {
		super(message);
	}

	public Throwables(String message,
	                  Throwable cause) {
		super(message,
		      cause);
		getThrowables().add(cause);
	}

	public Throwables(Throwable cause) {
		super(cause);
		getThrowables().add(cause);
	}

	public final Set<Throwable> getThrowables() {
		return throwables;
	}

	@Override
	public final String toString() {
		return throwables.toString();
	}
}
