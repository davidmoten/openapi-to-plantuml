package com.github.davidmoten.oas3.puml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.oas3.internal.model.PumlExtract;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

public final class ConverterMain {

    private ConverterMain() {
        // prevent instantiation
    }

    static DiagramDescription writeFileFormatFromPuml(String puml, String filename, String format)
            throws IOException {
        FileFormat fileFormat = FileFormat.valueOf(format);
        File file = new File(filename);
        SourceStringReader reader = new SourceStringReader(puml);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            // Write the first image to "os"
            return reader.outputImage(os, new FileFormatOption(fileFormat));
        }
    }

    private static final List<String> FILE_FORMATS = Lists.of("PUML", "EPS", "EPS_TEXT", "ATXT", "UTXT", "XMI_STANDARD",
            "XMI_STAR", "XMI_ARGO", "SCXML", "GRAPHML", "PDF", "MJPEG", "ANIMATED_GIF", "HTML", "HTML5", "VDX", "LATEX",
            "LATEX_NO_PREAMBLE", "BASE64", "BRAILLE_PNG", "PREPROC", "DEBUG", "PNG", "RAW", "SVG");

    public static void main(String[] args) throws IOException {
        String usage = "Usage: java -jar openapi-to-plantuml-all.jar (single|split)"
                + " <OPENAPI_FILE> <FILE_FORMAT> <OUTPUT_FILE>" + "\n  File formats are:\n"
                + FILE_FORMATS.stream().map(x -> "    " + x + "\n").collect(Collectors.joining());
        if (args.length != 4) {
            System.out.println(usage);
            throw new IllegalArgumentException("must pass 4 arguments");
        } else {
            String mode = args[0];
            if (mode.equals("split")) {
                String inputFilename = args[1];
                String format = args[2];
                File out = new File(args[3]);
                out.mkdirs();
                List<PumlExtract> list = Converter.openApiToPumlSplitByMethod(new File(inputFilename));
                for (PumlExtract puml : list) {
                    String filename = puml.classNameFrom().iterator().next().replace(" ", "_").replace("/", "_")
                            .replace("\\", "_").replace("{", "").replace("}", "");
                    if (format.equals("PUML")) {
                        File f = new File(out, filename + ".puml");
                        Files.write(f.toPath(), puml.puml().getBytes(StandardCharsets.UTF_8));
                    } else {
                        File f = new File(out, filename + "." + format.toLowerCase(Locale.ENGLISH));
                        writeFileFormatFromPuml(puml.puml(), f.getPath(), format);
//                        System.out.println("written " + f);
                    }
                }
            } else {
                String puml = Converter.openApiToPuml(new File(args[1]));
                String format = args[2];
                File out = new File(args[3]);
                if (format.equals("PUML")) {
                    Files.write(out.toPath(), puml.getBytes(StandardCharsets.UTF_8));
                } else {
                    writeFileFormatFromPuml(puml, out.getPath(), format);
                }
            }
        }
    }

}
