package com.github.davidmoten.oas3.puml;

import com.github.davidmoten.oas3.internal.ComponentsHelper;
import com.github.davidmoten.oas3.internal.Names;
import com.github.davidmoten.oas3.internal.PathsHelper;
import com.github.davidmoten.oas3.internal.Util;
import com.github.davidmoten.oas3.internal.model.Class;
import com.github.davidmoten.oas3.internal.model.*;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.davidmoten.oas3.internal.Util.quote;

@SuppressWarnings("UnnecessaryLocalVariable")
public final class Converter {

	public static final String PUML = "PUML";

	public static final Set<String> UNSUPPORTED_FORMATS
					= Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(FileFormat.ANIMATED_GIF.name(),
					                                                                FileFormat.BASE64.name(),
					                                                                FileFormat.HTML.name(),
					                                                                FileFormat.HTML5.name(),
					                                                                FileFormat.MJPEG.name(),
					                                                                FileFormat.PREPROC.name(),
					                                                                FileFormat.PDF.name(),
					                                                                PUML,
					                                                                FileFormat.SCXML.name(),
					                                                                FileFormat.XMI_ARGO.name(),
					                                                                FileFormat.XMI_STANDARD.name(),
					                                                                FileFormat.XMI_STAR.name())));

	public static final Set<String> SUPPORTED_FORMATS = Arrays.stream(FileFormat.values())
	                                                          .sorted(Comparator.comparing(Object::toString))
	                                                          .map(FileFormat::name)
	                                                          .filter(fileFormat -> !UNSUPPORTED_FORMATS.contains(fileFormat))
	                                                          .collect(Collectors.toCollection(LinkedHashSet::new));


	private static final FileFormat[] SUPPORTED_FILE_FORMAT_ARRAY        = Arrays.stream(FileFormat.values())
	                                                                             .sorted(Comparator.comparing(Object::toString))
	                                                                             .filter(Converter::isFileFormatSupported)
	                                                                             .collect(Collectors.toList())
	                                                                             .toArray(new FileFormat[]{});
	public static final  String       SUPPORTED_FILE_FORMAT_ARRAY_STRING = Arrays.stream(SUPPORTED_FILE_FORMAT_ARRAY)
	                                                                             .map(Converter::toString)
	                                                                             .collect(Collectors.joining());
	public static final  String       SUPPORTED_FORMATS_STRING           = SUPPORTED_FORMATS.toString();
	public static final  String       DELIMITER                          = ", ";
	private static final String       COLON                              = " : ";
	private static final String       SPACE                              = " ";

	private Converter() {
		// prevent instantiation
	}

	public static String toString(FileFormat fileFormat) {
		StringBuilder stringBuilder = new StringBuilder();
		String        name          = fileFormat.name();
		String        fileSuffix    = getFileSuffix(fileFormat);
		String        mimeType      = fileFormat.getMimeType();
		stringBuilder.append(name)
		             .append("\t*")
		             .append(fileSuffix)
		             .append("\t")
		             .append(mimeType)
		             .append("\n");
		return stringBuilder.toString();
	}

	public static void writeOpenApiToPumlAndTo(String openApiDirectoryFilePath,
	                                           String outputDirectoryPath,
	                                           String fileFormatsString)
					throws
					Throwables {
		Set<File>   openApiFiles    = toOpenApiFiles(openApiDirectoryFilePath);
		File        outputDirectory = toOutputDirectory(outputDirectoryPath);
		Set<String> fileFormats     = toFileFormats(fileFormatsString);
		Throwables throwables = writeOpenApiToPumlAndTo(openApiFiles,
		                                                outputDirectory,
		                                                fileFormats);
		if (!throwables.getThrowables()
		               .isEmpty()) {
			throw throwables;
		}
	}

	private static File toOutputDirectory(String outputDirectoryPath) {
		File outputDirectory = outputDirectoryPath == null || outputDirectoryPath.isEmpty()
		                       ? null
		                       : new File(outputDirectoryPath);
		return outputDirectory;
	}

	public static void writeOpenApiToPumlAndTo(String openApiDirectoryFilePath,
	                                           String outputDirectoryPath,
	                                           Set<String> fileFormats)
					throws
					Throwables {
		Set<File> openApiFiles    = toOpenApiFiles(openApiDirectoryFilePath);
		File      outputDirectory = toOutputDirectory(outputDirectoryPath);
		Throwables throwables = writeOpenApiToPumlAndTo(openApiFiles,
		                                                outputDirectory,
		                                                fileFormats);
		if (!throwables.getThrowables()
		               .isEmpty()) {
			throw throwables;
		}
	}

	public static void writeOpenApiToPumlAndTo(String openApiDirectoryFilePath,
	                                           String outputDirectoryPath,
	                                           FileFormat... fileFormatsArray)
					throws
					Throwables {
		Set<File>   openApiFiles    = toOpenApiFiles(openApiDirectoryFilePath);
		File        outputDirectory = toOutputDirectory(outputDirectoryPath);
		Set<String> fileFormats     = toFileFormats(fileFormatsArray);
		Throwables throwables = writeOpenApiToPumlAndTo(openApiFiles,
		                                                outputDirectory,
		                                                fileFormats);
		if (!throwables.getThrowables()
		               .isEmpty()) {
			throw throwables;
		}
	}

	public static void writeOpenApiDirectoryFileToPumlAndTo(File openApiDirectoryFile,
	                                                        File outputDirectory,
	                                                        Set<String> fileFormats)
					throws
					Throwables {
		Set<File> openApiFiles = toOpenApiFiles(openApiDirectoryFile);
		Throwables throwables = writeOpenApiToPumlAndTo(openApiFiles,
		                                                outputDirectory,
		                                                fileFormats);
		if (!throwables.getThrowables()
		               .isEmpty()) {
			throw throwables;
		}
	}

	public static void writeOpenApiDirectoryFileToPumlAndTo(File openApiDirectoryFile,
	                                                        File outputDirectory,
	                                                        FileFormat... fileFormatsArray)
					throws
					Throwables {
		Set<File>   openApiFiles = toOpenApiFiles(openApiDirectoryFile);
		Set<String> fileFormats  = toFileFormats(fileFormatsArray);
		Throwables throwables = writeOpenApiToPumlAndTo(openApiFiles,
		                                                outputDirectory,
		                                                fileFormats);
		if (!throwables.getThrowables()
		               .isEmpty()) {
			throw throwables;
		}
	}

	private static Set<File> toOpenApiFiles(String openApiDirectoryFilePath) {
		File      openApiDirectoryFile = new File(openApiDirectoryFilePath);
		Set<File> openApiFiles         = toOpenApiFiles(openApiDirectoryFile);
		return openApiFiles;
	}


	private static Set<File> toOpenApiFiles(File openApiDirectoryFile) {
		Set<File> openApiFiles = null;
		if (openApiDirectoryFile.isDirectory()) {
			File[] openApiFileArray = openApiDirectoryFile.listFiles(Converter::acceptOpenApiYamlFileAtDirectory);
			openApiFiles = toOpenApiFiles(openApiFileArray);
		} else if (acceptOpenApiYamlFile(openApiDirectoryFile)) {
			openApiFiles = new LinkedHashSet<>();
			openApiFiles.add(openApiDirectoryFile);
		}
		return openApiFiles;
	}

	private static boolean acceptOpenApiYamlFile(File openApiFile) {
		boolean acceptFile = acceptOpenApiYamlFileAtDirectory(openApiFile.getParentFile(),
		                                                      openApiFile.getName());
		return acceptFile;
	}

	private static boolean acceptOpenApiYamlFileAtDirectory(File dir,
	                                                        String name) {
		String  nameToLowerCase = name.toLowerCase(Locale.ROOT);
		boolean isYaml          = nameToLowerCase.endsWith(".yml") || nameToLowerCase.endsWith(".yaml");
		return isYaml;
	}

	private static Set<File> toOpenApiFiles(File... openApiFileArray) {
		Set<File> openApiFilesSet = openApiFileArray == null
		                            ? null
		                            : Arrays.stream(openApiFileArray)
		                                    .sorted(Comparator.comparing(File::getName))
		                                    .collect(Collectors.toCollection(LinkedHashSet::new));
		return openApiFilesSet;
	}

	private static Set<String> toFileFormats(String fileFormatsString) {
		Set<String> fileFormats = null;
		if (fileFormatsString != null) {
			String fileFormatsValuesString = fileFormatsString.contains("[") && fileFormatsString.contains("]")
			                                 ? fileFormatsString.substring(1,
			                                                               fileFormatsString.length() - 1)
			                                 : fileFormatsString;
			fileFormats = Arrays.stream(fileFormatsValuesString.split(DELIMITER))
			                    .collect(Collectors.toSet());
		}
		return fileFormats;
	}

	private static Set<String> toFileFormats(FileFormat... fileFormatsArray) {

		Set<String> fileFormats = null;
		if (fileFormatsArray != null && fileFormatsArray.length > 0) {
			fileFormats = Arrays.stream(fileFormatsArray)
			                    .map(Enum::name)
			                    .collect(Collectors.toSet());
		}
		return fileFormats;
	}

	private static Throwables writeOpenApiToPumlAndTo(Set<File> openApiFiles,
	                                                  File outputDirectory,
	                                                  Set<String> fileFormat) {
		Throwables     throwables             = new Throwables();
		Set<Throwable> openApiFilesThrowables = throwables.getThrowables();
		if (openApiFiles == null || openApiFiles.isEmpty()) {
			throw new IllegalArgumentException("Open Api Files are null or empty or contains no *.yml or *.yaml files");
		}
		for (File openApiFile : openApiFiles) {
			Throwables openApiFileThrowables = writeOpenApiToPumlAndTo(openApiFile,
			                                                           outputDirectory,
			                                                           fileFormat);
			if (!openApiFileThrowables.getThrowables()
			                          .isEmpty()) {
				openApiFilesThrowables.add(openApiFileThrowables);
			}
		}
		return throwables;
	}

	public static Throwables writeOpenApiToPumlAndTo(File openApiFile,
	                                                 File outputDirectory,
	                                                 Set<String> fileFormats) {
		createOutputDirectoryIfNeeded(outputDirectory);
		Throwables throwables      = new Throwables(openApiFile.getAbsolutePath());
		String     openApiFileName = openApiFile.getName();

		String openApiFileNameWithoutExtension = openApiFileName.substring(0,
		                                                                   openApiFileName.lastIndexOf("."));
		String         puml;
		Set<Throwable> openApiFileThrowables = throwables.getThrowables();
		try {
			puml = writeOpenApiToPuml(openApiFile,
			                          outputDirectory,
			                          openApiFileNameWithoutExtension);
		} catch (IOException ioException) {
			openApiFileThrowables.add(ioException);
			return throwables;
		}
		if (fileFormats != null && !fileFormats.isEmpty()) {
			for (String fileFormat : fileFormats) {
				try {
					writePumlTo(puml,
					            outputDirectory,
					            openApiFileNameWithoutExtension,
					            fileFormat);
				} catch (Exception exception) {
					openApiFileThrowables.add(exception);
				}
			}
		}
		return throwables;
	}

	public static boolean createOutputDirectoryIfNeeded(File outputDirectory) {
		if (outputDirectory == null) {
			throw new IllegalArgumentException("Output Directory is null or empty");
		}
		boolean mkdirs = outputDirectory.mkdirs();
		if (mkdirs) {
			info("Created new Directory: %s%n",
			     outputDirectory.getAbsolutePath());
		}
		if (!outputDirectory.isDirectory()) {
			throw new IllegalArgumentException("Output Directory is not a Directory");
		}
		return mkdirs;
	}

	public static String writeOpenApiToPuml(File openApiFile,
	                                        File outputDirectory,
	                                        String openApiFileNameWithoutExtension)
					throws
					IOException {
		String pumlFilePath = String.format("%s.puml",
		                                    openApiFileNameWithoutExtension);
		File pumlFile = new File(outputDirectory,
		                         pumlFilePath);
		info("Converting Open API File%n%s%nto PUML%n%s%n",
		     openApiFile.getAbsolutePath(),
		     pumlFile.getAbsolutePath());
		String puml = writeOpenApiToPuml(openApiFile,
		                                 pumlFile);
		return puml;
	}

	public static String writeOpenApiToPuml(File openApiFile,
	                                        File pumlFile)
					throws
					IOException {
		String puml = openApiToPuml(openApiFile);
		saveUTF(PUML,
		        puml,
		        pumlFile);
		return puml;
	}

	public static void saveUTF(String fileFormat,
	                           String utf8String,
	                           File outputFile)
					throws
					IOException {
		deletedIfExists(fileFormat,
		                outputFile);
		info("Saving %s File: %s%n",
		     fileFormat,
		     outputFile.getAbsolutePath());
		Files.write(outputFile.toPath(),
		            utf8String.getBytes(StandardCharsets.UTF_8));
		info("%s File Saved: %s%n",
		     fileFormat,
		     outputFile.getAbsolutePath());
	}

	public static DiagramDescription writePumlTo(String puml,
	                                             File outputDirectory,
	                                             String openApiFileNameWithoutExtension,
	                                             String fileFormat)
					throws
					IOException {
		if (PUML.equalsIgnoreCase(fileFormat)) {
			return null;
		}
		if (!isFileFormatSupported(fileFormat)) {
			String message = String.format("PUML cannot be saved as %s File",
			                               fileFormat);
			throw new IOException(message);
		}
		FileFormat fileFormatEnum = FileFormat.valueOf(fileFormat);
		String     fileSuffix     = getFileSuffix(fileFormatEnum);
		String outputFilePath = String.format("%s.puml%s",
		                                      openApiFileNameWithoutExtension,
		                                      fileSuffix);
		File outputFile = new File(outputDirectory,
		                           outputFilePath);
		FileFormatOption fileFormatOption = new FileFormatOption(fileFormatEnum);
		DiagramDescription diagramDescription = writePumlTo(puml,
		                                                    outputFile,
		                                                    fileFormatOption);
		return diagramDescription;
	}

	public static String getFileSuffix(FileFormat fileFormat) {
		String fileReplacementSuffix = String.format(".%s",
		                                             fileFormat.name()
		                                                       .toLowerCase(Locale.ROOT)
		                                                       .replaceAll("_",
		                                                                   "."));
		String fileFormatEnumSuffix = fileFormat.getFileSuffix();
		String fileSuffix = fileReplacementSuffix.equals(fileFormatEnumSuffix)
		                    ? fileFormatEnumSuffix
		                    : fileReplacementSuffix + fileFormatEnumSuffix;
		return fileSuffix;
	}

	static boolean isFileFormatSupported(FileFormat fileFormat) {
		boolean isFileFormatSupported = isFileFormatSupported(fileFormat.name());
		return isFileFormatSupported;
	}

	static boolean isFileFormatSupported(String fileFormat) {
		boolean isFileFormatSupported =
						!UNSUPPORTED_FORMATS.contains(fileFormat) && SUPPORTED_FORMATS.contains(fileFormat);
		return isFileFormatSupported;
	}

	private static DiagramDescription writePumlTo(String puml,
	                                              File outputFile,
	                                              FileFormatOption fileFormatOption)
					throws
					IOException {
		String fileFormat = fileFormatOption.getFileFormat()
		                                    .name();
		deletedIfExists(fileFormat,
		                outputFile);
		info("Saving %s File: %s%n",
		     fileFormat,
		     outputFile.getAbsolutePath());
		SourceStringReader sourceStringReader = new SourceStringReader(puml);
		try (OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
			DiagramDescription diagramDescription = sourceStringReader.outputImage(bufferedOutputStream,
			                                                                       fileFormatOption);
			info("%s File Saved: %s%n",
			     fileFormat,
			     outputFile.getAbsolutePath());
			info("%s Diagram Description: %s%n",
			     fileFormat,
			     diagramDescription.getDescription());
			return diagramDescription;
		} catch (IOException exception) {
			error("PUML cannot be saved as %s File: %s%n%s",
			      fileFormat,
			      outputFile.getAbsolutePath(),
			      exception);
			throw exception;
		}
	}

	public static boolean deletedIfExists(String fileFormat,
	                                      File outputFile) {
		if (outputFile.exists()) {
			info("Deleting old %s File: %s%n",
			     fileFormat,
			     outputFile.getAbsolutePath());
			boolean deleted = outputFile.delete();
			if (deleted) {
				info("Deleted old %s File: %s%n",
				     fileFormat,
				     outputFile.getAbsolutePath());
			}
			return deleted;
		}
		return false;
	}

	public static String openApiToPuml(InputStream in)
					throws
					IOException {
		String puml = openApiToPuml(IOUtils.toString(in,
		                                             StandardCharsets.UTF_8));
		return puml;
	}

	public static String openApiToPuml(File openApiFile)
					throws
					IOException {
		info("Open Api File to PUML: %s%n",
		     openApiFile.getAbsolutePath());
		try (InputStream openApiFileInputStream = new BufferedInputStream(new FileInputStream(openApiFile))) {
			String puml = openApiToPuml(openApiFileInputStream);
			return puml;
		}
	}

	public static String openApiToPuml(String openApi) {
		SwaggerParseResult result = new OpenAPIParser().readContents(openApi,
		                                                             null,
		                                                             null);
		if (result.getOpenAPI() == null) {
			throw new IllegalArgumentException("Not an OpenAPI definition");
		}
		return openApiToPuml(result.getOpenAPI());
	}

	private static String openApiToPuml(OpenAPI a) {

		Names names = new Names(a);
		Model model = ComponentsHelper //
		                               .toModel(names) //
		                               .add(PathsHelper.toModel(names));

		return "@startuml" //
		       + "\nhide <<" + toStereotype(ClassType.METHOD).get() + ">> circle" //
		       + "\nhide <<" + toStereotype(ClassType.RESPONSE).get() + ">> circle" //
		       + "\nhide <<" + toStereotype(ClassType.PARAMETER).get() + ">> circle" //
		       + "\nhide empty methods" //
		       + "\nhide empty fields" //
		       // make sure that periods in class names aren't interpreted as namespace
		       // separators (which results in recursive boxing)
		       + "\nset namespaceSeparator none" //
		       + toPlantUml(model) //
		       + "\n\n@enduml";
	}

	private static String toPlantUml(Model model) {
		int           anonNumber = 0;
		StringBuilder b          = new StringBuilder();
		for (Class cls : model.classes()) {
			b.append("\n\nclass " + Util.quote(cls.name()) + toStereotype(cls.type()).map(x -> " <<" + x + ">>")
			                                                                         .orElse("") + " {");
			cls.fields()
			   .stream()
			   .forEach(f -> {
				   b.append("\n  {field} " + f.name() + COLON + f.type() + (f.isRequired()
				                                                            ? ""
				                                                            : " {O}"));
			   });
			b.append("\n}");
		}

		for (Relationship r : model.relationships()) {
			if (r instanceof Association) {
				Association a = (Association) r;

				final String mult = toMultiplicity(a.type());

				final String label;
				final String arrow;
				if (a.responseCode()
				     .isPresent()) {
					arrow = "..>";
					label = a.responseCode()
					         .get() + a.responseContentType()
					                   .filter(x -> !"application/json".equalsIgnoreCase(x))
					                   .map(x -> SPACE + x)
					                   .orElse("");
				} else {
					arrow = "-->";
					label = a.propertyOrParameterName()
					         .orElse("");
				}
				b.append("\n\n"
				         + quote(a.from())
				         + SPACE
				         + arrow
				         + SPACE
				         + quote(mult)
				         + SPACE
				         + quote(a.to())
				         + (label.equals("")
				            ? ""
				            : SPACE + COLON + SPACE + quote(label)));
			} else {
				Inheritance a = (Inheritance) r;
				if (a.propertyName()
				     .isPresent() || a.type() != AssociationType.ONE) {
					String mult = toMultiplicity(a.type());
					anonNumber++;
					String diamond = "anon" + anonNumber;
					b.append("\n\ndiamond " + diamond);
					b.append("\n\n")
					 .append(quote(a.from()))
					 .append(SPACE)
					 .append("-->")
					 .append(quote(mult))
					 .append(SPACE)
					 .append(quote(diamond))
					 .append(a.propertyName()
					          .map(x -> COLON + quote(x))
					          .orElse(""));
					for (String otherClassName : a.to()) {
						b.append("\n\n")
						 .append(quote(otherClassName))
						 .append(SPACE)
						 .append("--|>")
						 .append(SPACE)
						 .append(quote(diamond));
					}
				} else {
					for (String otherClassName : a.to()) {
						b.append("\n\n")
						 .append(quote(otherClassName))
						 .append(SPACE)
						 .append("--|>")
						 .append(SPACE)
						 .append(quote(a.from()));
					}
				}
			}
		}
		return b.toString();
	}

	private static String toMultiplicity(AssociationType type) {
		final String mult;
		if (type == AssociationType.ONE) {
			mult = "1";
		} else if (type == AssociationType.ZERO_ONE) {
			mult = "0..1";
		} else {
			mult = "*";
		}
		return mult;
	}

	private static Optional<String> toStereotype(ClassType type) {
		final String result;
		if (type == ClassType.METHOD) {
			result = "Path";
		} else if (type == ClassType.PARAMETER) {
			result = "Parameter";
		} else if (type == ClassType.REQUEST_BODY) {
			result = "RequestBody";
		} else if (type == ClassType.RESPONSE) {
			result = "Response";
		} else {
			result = null;
		}
		return Optional.ofNullable(result);
	}


	static String getPropertyOrArg(String[] arguments,
	                               int index,
	                               String key,
	                               String defaultValue) {
		String argument = arguments.length > index
		                  ? arguments[index]
		                  : null;
		String property = System.getProperty(key,
		                                     null);
		String value = argument != null
		               ? argument
		               : property != null
		                 ? property
		                 : defaultValue;
		info("%s=%s Default Value=%s Argument[%d]=%s Property=%s%n",
		     key,
		     value,
		     defaultValue,
		     index,
		     argument,
		     property);
		return value;
	}

	static void info(String format,
	                 Object... params) {
		System.out.printf(format,
		                  params);
	}

	static void error(String format,
	                  Object... params) {
		System.err.printf(format,
		                  params);
	}

	static void error(Throwable throwable,
	                  String format,
	                  Object... params) {
		System.err.printf(format,
		                  params);
		throwable.printStackTrace();
	}
}
