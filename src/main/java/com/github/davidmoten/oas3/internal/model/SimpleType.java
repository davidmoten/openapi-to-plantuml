package com.github.davidmoten.oas3.internal.model;

public enum SimpleType {

	STRING("string"),
	DECIMAL("decimal"),
	INTEGER("integer"),
	BYTE_ARRAY("byte[]"),
	BOOLEAN("boolean"),
	DATE("date"),
	TIMESTAMP("timestamp"), //
	MAP("map"); // TODO get rid of Map once we model `additionalProperties`

	private final String name;

	SimpleType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
