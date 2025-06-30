package com.github.davidmoten.oas3.puml.gradle;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import com.github.davidmoten.oas3.puml.Converter;
import com.github.davidmoten.oas3.puml.Style;

public class OpenApiToPlantUmlPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        OpenApiToPlantUmlExtension extension = project.getExtensions()
            .create("openApiToPlantUml", OpenApiToPlantUmlExtension.class);

        project.getTasks().register("generatePlantUml", OpenApiToPlantUmlTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Generates PlantUML diagrams from an OpenAPI spec.");

            task.doFirst(t -> {
                task.setInput(extension.getInput());
                task.setStyle(extension.getStyle());
                task.setFormats(extension.getFormats());
                task.setOutput(extension.getOutput());
            });
        });
    }

class OpenApiToPlantUmlExtension {
    private String style = "SPLIT";
    private File input;
    private List<String> formats = List.of("SVG");
    private File output;

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }
}

abstract class OpenApiToPlantUmlTask extends DefaultTask {
    private String style;
    private File input;
    private List<String> formats = List.of("SVG");
    private File output;

    @Input
    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Input
    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    @Input
    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    @Optional
    @Input
    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    @TaskAction
    public void generate() {
        Style resolvedStyle = Style.valueOf(style.toUpperCase(Locale.ENGLISH));
        
        if (formats == null || formats.isEmpty()) {
            formats = Arrays.asList("PNG", "SVG");
        }

        for (String format : formats) {
            final File out;
            if (output == null) {
                if (resolvedStyle == Style.SINGLE) {
                    out = new File("build" + File.separator + "diagrams" + File.separator
                            + "class-diagram." + format.toLowerCase(Locale.ROOT));
                } else {
                    out = new File("build" + File.separator + "diagrams");
                }
            } else {
                out = output;
            }
            getLogger().info("Generating diagram in format=" + format //
                    + " with style=" + style + " to " + out);
            try {
                if (resolvedStyle == Style.SINGLE) {
                    Converter.writeSingleFile(input, format, out);
                } else {
                    Converter.writeSplitFiles(input, format, out);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Error generating diagrams", e);
            }
        }
    }
}
}
