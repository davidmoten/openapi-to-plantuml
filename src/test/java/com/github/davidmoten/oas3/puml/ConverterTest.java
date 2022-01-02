package com.github.davidmoten.oas3.puml;

import com.github.davidmoten.junit.Asserts;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;

public class ConverterTest {

	private static final File OPENAPI_EXAMPLE = new File("src/test/resources/openapi-example.yml");

	private static String readString(String filename)
					throws
					IOException {
		return new String(Files.readAllBytes(new File(filename).toPath()),
		                  StandardCharsets.UTF_8);
	}

	static void writeSvg(File openApiFile,
	                     String filename)
					throws
					IOException {
		try (InputStream in = new FileInputStream(openApiFile)) {
			String puml = Converter.openApiToPuml(in);
			writeSvgFromPuml(puml,
			                 filename);
		}
	}

	static void writeSvgFromPuml(String puml,
	                             String filename)
					throws
					IOException {
		File               file   = new File(filename);
		SourceStringReader reader = new SourceStringReader(puml);
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
			// Write the first image to "os"
			DiagramDescription result = reader.outputImage(os,
			                                               new FileFormatOption(FileFormat.SVG));
			System.out.println("  svg result: " + result.getDescription());
		}
	}

	public static void main(String[] args)
					throws
					IOException {
		writeSvg(new File(System.getProperty("user.home",
		                                     "") + "/imdb.yml"),
		         "target/imdb.svg");
	}

	@Test
	public void testIsUtility() {
		Asserts.assertIsUtilityClass(Converter.class);
	}

	@Test
	public void testConvert() {
		String openapi = "openapi: 3.0.1\n"
		                 + "components:\n"
		                 + "  schemas:\n"
		                 + "    CustomerType:\n"
		                 + "      type: string\n"
		                 + "      example: Example value\n"
		                 + "    Customer:\n"
		                 + "      properties:\n"
		                 + "        firstName:\n"
		                 + "          type: string\n"
		                 + "        lastName:\n"
		                 + "          type: string\n"
		                 + "        heightMetres:\n"
		                 + "          type: number\n"
		                 + "        type:\n"
		                 + "          $ref: '#/components/schemas/CustomerType'\n"
		                 + "        friends:\n"
		                 + "          type: array\n"
		                 + "          items:\n"
		                 + "            $ref: '#/components/schemas/Customer'\n"
		                 + "      ";

		Converter.openApiToPuml(openapi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertEmpty() {
		Converter.openApiToPuml("");
	}

	@Test
	public void testConvertPumlToSvg()
					throws
					IOException {
		writeSvg(OPENAPI_EXAMPLE,
		         "target/openapi-example.svg");
	}

	@Test
	@Ignore
	public void updateDocs()
					throws
					IOException {
		writeSvg(OPENAPI_EXAMPLE,
		         "src/docs/openapi-example.svg");
	}

	@Test
	public void testReadString()
					throws
					IOException {
		assertFalse(readString("src/test/resources/openapi-example.yml").isEmpty());
	}

	@Test
	public void generateExamplesMd()
					throws
					IOException {
		File          file = new File("src/docs/examples.md");
		StringBuilder b    = new StringBuilder();
		b.append("## openapi-to-plantuml examples\n");
		for (File f : new File("src/test/resources/inputs").listFiles()) {
			b.append("\n\n* [" + f.getName() + "](../../src/test/resources/inputs/" + f.getName() + ")");
			String svg = f.getName()
			              .substring(0,
			                         f.getName()
			                          .lastIndexOf(".")) + ".puml.svg";
			b.append("\n\n<img src=\"../../src/docs/tests/" + svg + "\"/>");
		}

		file.delete();
		Files.write(file.toPath(),
		            b.toString()
		             .getBytes(StandardCharsets.UTF_8));
	}
}
