package com.github.davidmoten.oas3.internal;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.oas3.internal.model.Class;
import com.github.davidmoten.oas3.internal.model.*;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import static com.github.davidmoten.oas3.internal.Util.first;
import static com.github.davidmoten.oas3.internal.Util.nullListToEmpty;

public final class PathsHelper {

	private PathsHelper() {
		// prevent instantiation
	}

	public static Model toModel(Names names) {
		return paths(names);
	}

	private static Model paths(Names names) {
		if (names.paths() == null) {
			return Model.EMPTY;
		} else {
			return names.paths() //
			            .entrySet() //
			            .stream() //
			            .map(entry -> toModelPath(entry.getKey(),
			                                      //
			                                      entry.getValue(),
			                                      names))
			            .reduce(Model.EMPTY,
			                    (a, b) -> a.add(b));
		}
	}

	private static Model toModelPath(String path,
	                                 PathItem p,
	                                 Names names) {
		// add method class blocks with HTTP verb and parameters
		// add response lines
		return p.readOperationsMap() //
		        .entrySet() //
		        .stream() //
		        .map(entry -> {
			        Operation operation = entry.getValue();
			        String    className = entry.getKey() + " " + path;
			        FieldsWithModel f = toModelParameters(names,
			                                              className,
			                                              operation.getParameters());
			        Model m = new Model(new Class(className,
			                                      ClassType.METHOD,
			                                      f.fields)).add(f.model);
			        m = m.add(toModelResponses(names,
			                                   operation,
			                                   className));
			        return m.add(toModelRequestBody(className,
			                                        operation,
			                                        names));
		        }) //
		        .reduce(Model.EMPTY,
		                (a, b) -> a.add(b));
	}

	private static FieldsWithModel toModelParameters(Names names,
	                                                 String className,
	                                                 List<Parameter> parameters) {
		return nullListToEmpty(parameters) //
		                                   .stream()//
		                                   .map(param -> toModelParameter(names,
		                                                                  className,
		                                                                  param)) //
		                                   .reduce(FieldsWithModel.EMPTY,
		                                           (a, b) -> a.add(b));
	}

	private static FieldsWithModel toModelParameter(Names names,
	                                                String className,
	                                                Parameter param) {
		String  ref           = param.get$ref();
		String  parameterName = param.getName();
		Boolean required      = param.getRequired();
		if (ref != null) {
			// resolve the parameter ref chain so we can get the parameter `name` and
			// `required` fields
			Parameter p = param;
			while (p.get$ref() != null) {
				String r = p.get$ref();
				p = getParameter(names.components(),
				                 r);
			}
			// override name with name from last ref
			parameterName = p.getName();
			required = p.getRequired();
		}
		if (ref != null) {
			Model model = new Model(Association //
			                                    .from(className) //
			                                    .to(names.refToClassName(ref)) //
			                                    .one() //
			                                    .propertyOrParameterName(parameterName) //
			                                    .build());
			return new FieldsWithModel(Collections.emptyList(),
			                           model);
		} else {
			Optional<Field> field = Optional.empty();
			final Model     model;
			final Optional<String> type = Common.getUmlTypeName(param.get$ref(),
			                                                    param.getSchema(),
			                                                    names);
			if (type.isPresent() && Common.isSimpleType(type.get())) {
				field = Optional.of(new Field(parameterName,
				                              type.get(),
				                              type.get()
				                                  .endsWith("]"),
				                              required));
				model = Model.EMPTY;
			} else {
				String      anonClassName = names.nextClassName(className + "." + parameterName);
				final Model m;
				if (param.getSchema() != null) {
					m = Common.toModelClass(anonClassName,
					                        param.getSchema(),
					                        names,
					                        ClassType.PARAMETER);
				} else {
					m = new Model(new Class(anonClassName,
					                        ClassType.PARAMETER));
				}
				model = m.add(Association.from(className)
				                         .to(anonClassName)
				                         .type(required
				                               ? AssociationType.ONE
				                               : AssociationType.ZERO_ONE)
				                         .propertyOrParameterName(parameterName)
				                         .build());
			}
			// TODO else get schema from content?

			return new FieldsWithModel(field.map(x -> Collections.singletonList(x))
			                                .orElse(Collections.emptyList()),
			                           model);
		}

	}

	private static Parameter getParameter(Components components,
	                                      String ref) {
		Preconditions.checkNotNull(ref);
		Reference r = new Reference(ref);
		if ("#/components/parameters".equals(r.namespace)) {
			return components.getParameters()
			                 .get(r.simpleName);
		} else {
			throw new RuntimeException("unexpected");
		}
	}

	private static Model toModelRequestBody(String className,
	                                        Operation operation,
	                                        Names names) {
		RequestBody body = operation.getRequestBody();
		if (body != null) {
			String ref = body.get$ref();
			if (ref != null) {
				return new Model(Association.from(className)
				                            .to(names.refToClassName(ref))
				                            .one()
				                            .build());
			}
			Content content = body.getContent();
			if (content != null) {
				Entry<String, MediaType> mediaType = first(content).get();
				// use the first content entry
				final String requestBodyClassName;
				final Model  model;
				Schema<?> sch = mediaType.getValue()
				                         .getSchema();
				// note that sch cannot be null because the parser sets the
				// response body to null if schema is missing
				if (sch.get$ref() != null) {
					requestBodyClassName = names.refToClassName(sch.get$ref());
					model = Model.EMPTY;
				} else {
					requestBodyClassName = className + " Request";
					model = Common.toModelClass(requestBodyClassName,
					                            sch,
					                            names,
					                            ClassType.REQUEST_BODY);
				}
				Association a = Association.from(className)
				                           .to(requestBodyClassName)
				                           .one()
				                           .build();
				return model.add(a);
			}
		}
		return Model.EMPTY;
	}

	private static Model toModelResponses(Names names,
	                                      Operation operation,
	                                      String className) {
		return operation //
		                 .getResponses() //
		                 .entrySet() //
		                 .stream() //
		                 .map(ent -> {
			                 String responseCode = ent.getKey();
			                 // TODO only using the first content
			                 ApiResponse r = ent.getValue();
			                 final Model model;
			                 if (r.get$ref() != null) {
				                 String returnClassName = names.refToClassName(r.get$ref());
				                 model = new Model(Association.from(className)
				                                              .to(returnClassName)
				                                              .one()
				                                              .responseCode(responseCode)
				                                              .build());
			                 } else {

				                 if (r.getContent() == null) {
					                 final String newReturnClassName = className + " " + responseCode;
					                 Model m = new Model(new Class(newReturnClassName,
					                                               ClassType.RESPONSE));
					                 String returnClassName = newReturnClassName;
					                 model = m.add(Association.from(className)
					                                          .to(returnClassName)
					                                          .one()
					                                          .responseCode(responseCode)
					                                          .build());
				                 } else {
					                 Model m = Model.EMPTY;
					                 for (Entry<String, MediaType> contentEntry : r.getContent()
					                                                               .entrySet()) {
						                 String contentType = contentEntry.getKey();
						                 final String newReturnClassName = className
						                                                   + " "
						                                                   + responseCode
						                                                   + ("application/json".equals(contentType)
						                                                      ? ""
						                                                      : " " + contentType);
						                 MediaType mediaType = contentEntry.getValue();
						                 Schema<?> sch       = mediaType.getSchema();
						                 if (sch == null) {
							                 // TODO
							                 System.out.println("TODO handle null schema in response");
						                 } else if (sch.get$ref() != null) {
							                 String returnClassName = names.refToClassName(sch.get$ref());
							                 m = m.add(Association.from(className)
							                                      .to(returnClassName)
							                                      .one()
							                                      .responseCode(responseCode)
							                                      .responseContentType(contentType)
							                                      .build());
						                 } else {
							                 String returnClassName = newReturnClassName;
							                 m = m.add(Common.toModelClass(returnClassName,
							                                               sch,
							                                               names,
							                                               ClassType.RESPONSE));
							                 m = m.add(Association.from(className)
							                                      .to(returnClassName)
							                                      .one()
							                                      .responseCode(responseCode)
							                                      .responseContentType(contentType)
							                                      .build());
						                 }

					                 }
					                 model = m;
				                 }
			                 }
			                 return model;
		                 })
		                 .reduce(Model.EMPTY,
		                         (a, b) -> a.add(b));
	}

	private static final class FieldsWithModel {
		private static final FieldsWithModel EMPTY = new FieldsWithModel(Collections.emptyList(),
		                                                                 Model.EMPTY);
		private final        List<Field>     fields;
		private final        Model           model;

		FieldsWithModel(List<Field> fields,
		                Model model) {
			this.fields = fields;
			this.model = model;
		}

		FieldsWithModel add(FieldsWithModel f) {
			List<Field> list = new ArrayList<>(fields);
			list.addAll(f.fields);
			return new FieldsWithModel(list,
			                           model.add(f.model));
		}
	}

	private static final class Reference {
		private final String namespace;
		private final String simpleName;

		private Reference(String ref) {
			this.namespace = ref.substring(0,
			                               ref.lastIndexOf("/"));
			this.simpleName = ref.substring(ref.lastIndexOf("/") + 1);
		}
	}

}
