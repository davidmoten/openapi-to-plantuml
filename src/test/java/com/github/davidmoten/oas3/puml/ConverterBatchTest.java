package com.github.davidmoten.oas3.puml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ConverterBatchTest {

	private static final File INPUTS  = new File("src/test/resources/inputs/");
	private static final File OUTPUTS = new File("src/test/resources/outputs/");

	private final File input;

	public ConverterBatchTest(File input) {
		this.input = input;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<?> files() {
		File[] list = INPUTS.listFiles();
		if (list == null) {
			return Collections.emptyList();
		} else {
			List<File> result = Arrays.asList(list);
			Collections.sort(result,
			                 (a, b) -> a.getName()
			                            .compareTo(b.getName()));
			return result;
		}
	}

	@Test
	public void test() {
		System.out.println("checking " + input);
		try (InputStream in = new FileInputStream(input)) {
			String puml = Converter.openApiToPuml(in)
			                       .trim();
			File pumlFile = new File("target/outputs",
			                         input.getName()
			                              .substring(0,
			                                         input.getName()
			                                              .lastIndexOf('.')) + ".puml");
			pumlFile.getParentFile()
			        .mkdirs();
			pumlFile.delete();
			Files.write(pumlFile.toPath(),
			            puml.getBytes(StandardCharsets.UTF_8));
			File output = new File(OUTPUTS,
			                       input.getName()
			                            .substring(0,
			                                       input.getName()
			                                            .lastIndexOf('.')) + ".puml");
			if (!output.exists()) {
				output.createNewFile();
				System.out.println(puml);
			}
			String expected = com.github.davidmoten.junit.Files.readUtf8(output)
			                                                   .trim();
			ConverterTest.writeSvg(input,
			                       "target/outputs/" + output.getName() + ".svg");
			assertEquals(expected,
			             puml);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
