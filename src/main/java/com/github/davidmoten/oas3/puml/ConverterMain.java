package com.github.davidmoten.oas3.puml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

public final class ConverterMain {

    private ConverterMain() {
        // prevent instantiation
    }

    static DiagramDescription writeFileFormatFromPuml(String puml, String filename,
            FileFormat fileFormat) throws IOException {
        File file = new File(filename);
        SourceStringReader reader = new SourceStringReader(puml);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            // Write the first image to "os"
            return reader.outputImage(os, new FileFormatOption(fileFormat));
        }
    }

    public static void main(String[] args) throws IOException {
        String usage = "Usage: java -jar openapi-to-plantuml-all.jar <OPENAPI_FILE> <FILE_FORMAT> <OUTPUT_FILE> <SHOW_NOTE{0|1}>"
                + "\n  File formats are:\n    PUML\n" + Arrays.stream(FileFormat.values())
                        .map(x -> "    " + x + "\n").collect(Collectors.joining());
        if ((args.length != 3) || (args.length != 4)) {
            System.out.println(usage);
            throw new IllegalArgumentException("must pass 3 or 4 arguments");
        } else {
            boolean showNote = false;
            if (args.length == 4) {
                showNote = (args[4] == "1") ? true : false;
            }
            String puml = Converter.openApiToPuml(new File(args[0]), showNote);
            String format = args[1];
            File out = new File(args[2]);
            if (format.equals("PUML")) {
                Files.write(out.toPath(), puml.getBytes(StandardCharsets.UTF_8));
            } else {
                FileFormat ff = FileFormat.valueOf(format);
                writeFileFormatFromPuml(puml, out.getPath(), ff);
            }
        }
    }

}
