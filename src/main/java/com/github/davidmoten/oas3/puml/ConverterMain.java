package com.github.davidmoten.oas3.puml;

import com.github.davidmoten.oas3.internal.model.Throwables;

public final class ConverterMain {

	public static final String JAVA_JAR = "java -jar openapi-to-plantuml-all.jar <OPENAPI_YAML> <OUTPUT_DIRECTORY> "
	                                      + "<FILE_FORMAT>|<[FILE_FORMAT1, FILE_FORMAT1...]>\n";
	public static final String USAGE    = JAVA_JAR
	                                      + "<OPENAPI_YAML> file or Directory containing *.yml or *.yaml files\n"
	                                      + "<OUTPUT_DIRECTORY> output Directory\n"
	                                      + "<FILE_FORMAT> optional file format default PUML and SVG only\n"
	                                      + " or"
	                                      + "<[FILE_FORMAT1, FILE_FORMAT1...]> optional several file formats\n"
	                                      + "surrounded by [delimited by comma and space ', ')] i.e. supported formats "
	                                      + "are:\n"
	                                      + Converter.SUPPORTED_FORMATS_STRING
	                                      + "\n"
	                                      + Converter.SUPPORTED_FILE_FORMAT_ARRAY_STRING;

	private ConverterMain() {
		// prevent instantiation
	}

	public static void main(String[] arguments)
					throws
					Throwables {
		try {
			if (arguments.length < 2) {
				throw new IllegalArgumentException("must pass 2-3 arguments");
			}
			String openApiFilePath = Converter.getPropertyOrArg(arguments,
			                                                    0,
			                                                    "OPENAPI_YAML",
			                                                    "./openapi.yaml");
			String outputDirectoryPath = Converter.getPropertyOrArg(arguments,
			                                                        1,
			                                                        "OUTPUT_DIRECTORY",
			                                                        ".");
			String fileFormatsString = Converter.getPropertyOrArg(arguments,
			                                                      2,
			                                                      "FILE_FORMAT",
			                                                      "SVG");
			Converter.writeOpenApiToPumlAndTo(openApiFilePath,
			                                  outputDirectoryPath,
			                                  fileFormatsString);
		} catch (Exception | Throwables throwable) {
			Converter.error(throwable,
			                "%nUsage:%n%s%n",
			                USAGE);
			throw throwable;
		}
	}
}
