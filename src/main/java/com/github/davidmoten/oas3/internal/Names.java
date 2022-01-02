package com.github.davidmoten.oas3.internal;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.*;

import static com.github.davidmoten.oas3.internal.Util.nullMapToEmpty;

public final class Names {

	private static final String              EMPTY_RESPONSE_CLASS_NAME = "Empty Response";
	private final        Map<String, String> refClassNames             = new HashMap<>();
	private final        Set<String>         classNames                = Sets.newHashSet(EMPTY_RESPONSE_CLASS_NAME);
	private final        OpenAPI             openapi;

	public Names(OpenAPI a) {
		Components components = a.getComponents();
		this.openapi = a;
		if (components != null) {
			// resolve name clashes
			nullMapToEmpty(components.getSchemas()).keySet()
			                                       .forEach(name -> {
				                                       String className = nextClassName(classNames,
				                                                                        name);
				                                       refClassNames.put("#/components/schemas/" + name,
				                                                         className);
			                                       });
			nullMapToEmpty(components.getRequestBodies()).keySet()
			                                             .forEach(name -> {
				                                             String className = nextClassName(classNames,
				                                                                              name);
				                                             refClassNames.put("#/components/requestBodies/" + name,
				                                                               className);
			                                             });
			nullMapToEmpty(components.getParameters()).keySet()
			                                          .forEach(name -> {
				                                          String className = nextClassName(classNames,
				                                                                           name);
				                                          refClassNames.put("#/components/parameters/" + name,
				                                                            className);
			                                          });
			nullMapToEmpty(components.getResponses()).keySet()
			                                         .forEach(name -> {
				                                         String className = nextClassName(classNames,
				                                                                          name);
				                                         refClassNames.put("#/components/responses/" + name,
				                                                           className);
			                                         });
		}
	}

	private static String nextClassName(Set<String> classNames,
	                                    String... candidates) {
		return nextClassName(classNames,
		                     Arrays.asList(candidates));
	}

	private static String nextClassName(Set<String> classNames,
	                                    List<String> candidates) {
		Preconditions.checkArgument(!candidates.isEmpty());
		for (String candidate : candidates) {
			if (!classNames.contains(candidate)) {
				classNames.add(candidate);
				return candidate;
			}
		}
		int    i             = 1;
		String lastCandidate = candidates.get(candidates.size() - 1);
		while (true) {
			String className = lastCandidate + "." + i;
			if (!classNames.contains(className)) {
				classNames.add(className);
				return className;
			}
			i++;
		}
	}

	String schemaClassName(String simpleName) {
		return refToClassName("#/components/schemas/" + simpleName);
	}

	String requestBodyClassName(String simpleName) {
		return refToClassName("#/components/requestBodies/" + simpleName);
	}

	String responseClassName(String simpleName) {
		return refToClassName("#/components/responses/" + simpleName);
	}

	private String parameterClassName(String simpleName) {
		return refToClassName("#/components/parameters/" + simpleName);
	}

	String requestBodyClassName(RequestBody b) {
		return nullMapToEmpty(components().getRequestBodies()) //
		                                                       .entrySet() //
		                                                       .stream() //
		                                                       .filter(entry -> entry.getValue() == b) //
		                                                       .map(entry -> requestBodyClassName(entry.getKey())) //
		                                                       .findFirst() //
		                                                       .orElseThrow(() -> new RuntimeException("cound not find "
		                                                                                               + b));
	}

	String parameterClassName(Parameter p) {
		return nullMapToEmpty(components().getParameters()) //
		                                                    .entrySet() //
		                                                    .stream() //
		                                                    .filter(entry -> entry.getValue() == p) //
		                                                    .map(entry -> parameterClassName(entry.getKey())) //
		                                                    .findFirst() //
		                                                    .orElseThrow(() -> new RuntimeException("cound not find " + p));
	}

	String refToClassName(String ref) {
		Preconditions.checkNotNull(ref);
		String className = refClassNames.get(ref);
		if (className == null) {
			throw new RuntimeException("could not find ref=" + ref);
		} else {
			return className;
		}
	}

	Components components() {
		return openapi.getComponents();
	}

	Paths paths() {
		return openapi.getPaths();
	}

	String nextClassName(String candidate) {
		return nextClassName(classNames,
		                     candidate);
	}

	@SuppressWarnings("rawtypes")
	Map<String, Schema> schemas() {
		if (openapi.getComponents() == null) {
			return Collections.emptyMap();
		} else {
			return nullMapToEmpty(openapi.getComponents()
			                             .getSchemas());
		}
	}

	Map<String, RequestBody> requestBodies() {
		if (openapi.getComponents() == null) {
			return Collections.emptyMap();
		} else {
			return nullMapToEmpty(openapi.getComponents()
			                             .getRequestBodies());
		}
	}

	Map<String, Parameter> parameters() {
		if (openapi.getComponents() == null) {
			return Collections.emptyMap();
		} else {
			return nullMapToEmpty(openapi.getComponents()
			                             .getParameters());
		}
	}

	Map<String, ApiResponse> responses() {
		if (openapi.getComponents() == null) {
			return Collections.emptyMap();
		} else {
			return nullMapToEmpty(openapi.getComponents()
			                             .getResponses());
		}
	}
}
