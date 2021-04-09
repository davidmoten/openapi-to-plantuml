package com.github.davidmoten.oas3.puml;

public final class ConverterMain {

    private ConverterMain() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        String usage = "Usage: java -jar openapi-to-plantuml-all.jar <OPENAPI_FILE> (SVG|JPG|PUML) ><OUTPUT_FILE>";
        System.out.println(usage);
    }

}
