package org.davidmoten.oa3.plantuml.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.davidmoten.oas3.puml.ConverterMain;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = false)
public final class GenerateMojo extends AbstractMojo {

    @Parameter(name = "style", defaultValue = "SINGLE")
    private Style style;

    @Parameter(name = "input", required = true)
    private File input;

    /**
     * Possible values are: "PUML", "EPS", "EPS_TEXT", "ATXT", "UTXT",
     * "XMI_STANDARD", "XMI_STAR", "XMI_ARGO", "SCXML", "GRAPHML", "PDF", "MJPEG",
     * "ANIMATED_GIF", "HTML", "HTML5", "VDX", "LATEX", "LATEX_NO_PREAMBLE",
     * "BASE64", "BRAILLE_PNG", "PREPROC", "DEBUG", "PNG", "RAW", "SVG"
     */
    @Parameter(name = "formats")
    private List<String> formats;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/diagrams")
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        if (formats == null || formats.isEmpty()) {
            formats = Arrays.asList("PNG", "SVG");
        }
        for (String format : formats) {
            try {
                ConverterMain.main(new String[] { //
                        style.name().toLowerCase(Locale.ROOT), //
                        input.getAbsolutePath(), //
                        format, //
                        outputDirectory.getAbsolutePath() });
            } catch (IOException e) {
                throw new MojoExecutionException("Error generating diagrams", e);
            }
        }
    }
}
