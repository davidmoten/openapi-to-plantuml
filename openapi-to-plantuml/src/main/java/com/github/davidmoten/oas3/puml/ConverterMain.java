package com.github.davidmoten.oas3.puml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Lists;

public final class ConverterMain {

    private ConverterMain() {
        // prevent instantiation
    }

    private static final List<String> FILE_FORMATS = Lists.of("PUML", "EPS", "EPS_TEXT", "ATXT", "UTXT", "XMI_STANDARD",
            "XMI_STAR", "XMI_ARGO", "SCXML", "GRAPHML", "PDF", "MJPEG", "ANIMATED_GIF", "HTML", "HTML5", "VDX", "LATEX",
            "LATEX_NO_PREAMBLE", "BASE64", "BRAILLE_PNG", "PREPROC", "DEBUG", "PNG", "RAW", "SVG");

    public static void main(String[] args) throws IOException {
        String usage = "Usage: java -jar openapi-to-plantuml-all.jar (single|split)"
                + " <OPENAPI_FILE> <FILE_FORMAT> <OUTPUT_DIRECTORY> [include-relation-fields]"
                + "\n  File formats are:\n"
                + FILE_FORMATS.stream().map(x -> "    " + x + "\n").collect(Collectors.joining());
        if (args.length != 4 && args.length != 5) {
            System.out.println(usage);
            throw new IllegalArgumentException("must pass 4 or 5 arguments");
        } else {
            Style style = Style.valueOf(args[0].toUpperCase(Locale.ENGLISH));
            String inputFilename = args[1];
            String format = args[2];
            File out = new File(args[3]);
            boolean includeRelationFields = false;
            if (args.length == 5) {
                includeRelationFields = Boolean.parseBoolean(args[4]);
            }
            if (style == Style.SPLIT) {
                out.mkdirs();
                Converter.writeSplitFiles(new File(inputFilename), format, out, includeRelationFields);
            } else {
                Converter.writeSingleFile(new File(inputFilename), format, out, includeRelationFields);
            }
        }
    }
}
