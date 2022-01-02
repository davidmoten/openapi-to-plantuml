package com.github.davidmoten.oas3.puml;

import com.github.davidmoten.oas3.internal.model.Throwables;
import net.sourceforge.plantuml.FileFormat;
import org.junit.Test;

import java.io.File;

public class DemoBatchTest {

	private static final String OPEN_API_DIRECTORY      = "src/test/resources/demo/";
	public static final  File   OPEN_API_DIRECTORY_FILE = new File(OPEN_API_DIRECTORY);
	private static final String OUTPUT_DIRECTORY        = "target/converted-puml/demo";
	public static final  File   OUTPUT_DIRECTORY_FILE   = new File(OUTPUT_DIRECTORY);

	@Test
	public void testWriteOpenApiDirectoryFilePathsToPumlAndToFileFormatSet()
					throws
					Throwables {
		if (!"true".equalsIgnoreCase(System.getProperty("demo",
		                                                "true"))) {
			return;
		}
		Converter.writeOpenApiToPumlAndTo(OPEN_API_DIRECTORY,
		                                  OUTPUT_DIRECTORY,
		                                  Converter.SUPPORTED_FORMATS);
	}


	@Test
	public void testWriteOpenApiDirectoryFilesToFileFormatSet()
					throws
					Throwables {
		if (!"true".equalsIgnoreCase(System.getProperty("demo",
		                                                "true"))) {
			return;
		}
		Converter.writeOpenApiDirectoryFileToPumlAndTo(OPEN_API_DIRECTORY_FILE,
		                                               OUTPUT_DIRECTORY_FILE,
		                                               Converter.SUPPORTED_FORMATS);
	}

	@Test
	public void testWriteOpenApiDirectoryFilePathsFileFormatArraySVGAndPNG()
					throws
					Throwables {
		if (!"true".equalsIgnoreCase(System.getProperty("demo",
		                                                "true"))) {
			return;
		}
		Converter.writeOpenApiToPumlAndTo(OPEN_API_DIRECTORY,
		                                  OUTPUT_DIRECTORY,
		                                  FileFormat.SVG,
		                                  FileFormat.PNG);
	}

	@Test
	public void testWriteOpenApiDirectoryFilesFileFormatArraySVGAndPNG()
					throws
					Throwables {
		if (!"true".equalsIgnoreCase(System.getProperty("demo",
		                                                "true"))) {
			return;
		}
		Converter.writeOpenApiDirectoryFileToPumlAndTo(OPEN_API_DIRECTORY_FILE,
		                                               OUTPUT_DIRECTORY_FILE,
		                                               FileFormat.SVG,
		                                               FileFormat.PNG);
	}

}
