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
import org.apache.maven.project.MavenProject;

import com.github.davidmoten.oas3.puml.ConverterMain;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = false)
public final class GenerateMojo extends AbstractMojo {

    @Parameter(name = "style", defaultValue = "SPLIT")
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

    @Parameter(name = "output")
    private File output;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        if (formats == null || formats.isEmpty()) {
            formats = Arrays.asList("PNG", "SVG");
        }

        for (String format : formats) {
            final File out;
            if (output == null) {
                if (style == Style.SINGLE) {
                    out = new File(project.getBuild().getDirectory() + File.pathSeparator + "diagrams"
                            + File.pathSeparator + "class-diagram." + format.toLowerCase(Locale.ROOT));
                } else {
                    out = new File(project.getBuild().getDirectory() + File.pathSeparator + "diagrams");
                }
            } else {
                out = output;
            }
            getLog().info("Generating diagram in format=" + format + " with style=" + style + " to " + output);
            try {
                ConverterMain.main(new String[] { //
                        style.name().toLowerCase(Locale.ROOT), //
                        input.getAbsolutePath(), //
                        format, //
                        out.getAbsolutePath() });
            } catch (IOException e) {
                throw new MojoExecutionException("Error generating diagrams", e);
            }
        }
    }
}
