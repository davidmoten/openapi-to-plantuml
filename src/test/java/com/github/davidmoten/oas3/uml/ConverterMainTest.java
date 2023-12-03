package com.github.davidmoten.oas3.uml;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.junit.Asserts;

import net.sourceforge.plantuml.FileFormat;

public class ConverterMainTest {

    @Test
    public void testToImages() throws IOException {
        for (FileFormat ff : new FileFormat[] { FileFormat.PNG, FileFormat.SVG, FileFormat.LATEX }) {
            try {
                String[] args = new String[] { "single", "src/test/resources/openapi-example.yml", ff.toString(),
                        new File("target/converted." + ff.getFileSuffix()).getPath() };
                ConverterMain.main(args);
            } catch (Throwable e) {
                //
            }
        }
    }

    @Test
    public void testSplit() throws IOException {
        for (FileFormat ff : new FileFormat[] { FileFormat.PNG, FileFormat.SVG, FileFormat.LATEX }) {
            System.out.println("writing unqork split in format " + ff);
            String[] args = new String[] { "split", "src/test/resources/demo/unqork.yml", ff.toString(),
                    new File("target/unqork-" + ff.name().toLowerCase(Locale.ENGLISH)).getPath() };
            ConverterMain.main(args);
        }
    }
    
    @Test
    public void testSplitMermaid() throws IOException {
        List<String> formats = Lists.of("PUML", "MERMAID");
        for (String format: formats) {
            System.out.println("writing unqork split in format " + format);
            String[] args = new String[] { "split", "src/test/resources/demo/unqork.yml", format,
                    new File("target/unqork-" + format.toLowerCase(Locale.ENGLISH)).getPath() };
            ConverterMain.main(args);
        }
        
        File md = new File("target/mermaids.html");
        try (PrintStream out = new PrintStream(md)) {
            out.println("<html>");
            out.println("<body>");
            File mermaids = new File("target/unqork-mermaid");
            for (File f: mermaids.listFiles()) {
                String code = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                out.println();
                out.println("<h3>"+ f.getName().replace(".mermaid", "") + "</h3>");
                out.println("<pre class=\"mermaid\">");
                out.println(StringEscapeUtils.escapeHtml4(code));
                out.println("</pre>");
            }
            out.println("<script type=\"module\">\n"
                    + "      import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';\n"
                    + "      mermaid.initialize({ startOnLoad: true, theme: 'neutral', securityLevel: 'loose' });\n"
                    + "    </script>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Test
    public void testSplitEgcApiProduceImages() throws IOException {
            long t = System.currentTimeMillis();
            for (FileFormat ff : new FileFormat[] {FileFormat.PNG, FileFormat.SVG}) {
                System.out.println("writing egc-api split in format " + ff);
                String[] args = new String[] { "split", "src/test/resources/other/egc-api.yml", ff.toString(),
                        new File("target/egc-api-" + ff.name().toLowerCase(Locale.ENGLISH)).getPath() };
                ConverterMain.main(args);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("written egc-api in " + t + "ms");
    }

    @Test
    public void testToPuml() throws IOException {
        String[] args = new String[] { "single", "src/test/resources/openapi-example.yml", "PUML",
                "target/converted.puml" };
        ConverterMain.main(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNumberOfArgs() throws IOException {
        ConverterMain.main(new String[] { "a" });
    }

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(ConverterMain.class);
    }
    

}
