package com.example.openapi;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.DefaultTask;

import com.github.davidmoten.oas3.puml.Converter;
import com.github.davidmoten.oas3.puml.Style;

import java.io.File;
import java.util.List;

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
        Style resolvedStyle = Style.valueOf(style.toUpperCase());
        File resolvedOutput = output != null ? output : new File("build/diagrams");
        if (!resolvedOutput.exists()) {
            resolvedOutput.mkdirs();
        }

        Converter converter = new Converter()
            .input(input)
            .style(resolvedStyle)
            .formats(formats)
            .output(resolvedOutput);

        converter.convert();
    }
}
}
