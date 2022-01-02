package com.github.davidmoten.oas3.internal.model;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;

public final class Class {
	private final String      name;
	private final ClassType   type;
	private final List<Field> fields;

	public Class(String name,
	             ClassType type,
	             List<Field> fields) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(fields);
		this.name = name;
		this.type = type;
		this.fields = fields;
	}

	public Class(String name,
	             ClassType type) {
		this(name,
		     type,
		     Collections.emptyList());
	}

	public String name() {
		return name;
	}

	public ClassType type() {
		return type;
	}

	public List<Field> fields() {
		return fields;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Class [name=");
		b.append(name);
		b.append(", type=");
		b.append(type);
		b.append(", fields=");
		b.append(fields);
		b.append("]");
		return b.toString();
	}

}
