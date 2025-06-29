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
                + " <OPENAPI_FILE> <FILE_FORMAT> <OUTPUT_DIRECTORY>" + "\n  File formats are:\n"
                + FILE_FORMATS.stream().map(x -> "    " + x + "\n").collect(Collectors.joining());
        if (args.length != 4) {
            System.out.println(usage);
            throw new IllegalArgumentException("must pass 4 arguments");
        } else {
            Style style = Style.valueOf(args[0].toUpperCase(Locale.ENGLISH));
            if (style == Style.SPLIT) {
                String inputFilename = args[1];
                String format = args[2];
                File out = new File(args[3]);
                out.mkdirs();
                Converter.writeSplitFiles(new File(inputFilename), format, out);
            } else {
                String inputFilename = args[1];
                String format = args[2];
                File out = new File(args[3]);
                Converter.writeSingleFile(new File(inputFilename), format, out);
            }
        }
    }
}
