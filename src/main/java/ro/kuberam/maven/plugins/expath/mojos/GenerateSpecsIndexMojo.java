package ro.kuberam.maven.plugins.expath.mojos;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.transform.sax.SAXSource;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.xml.sax.InputSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import ro.kuberam.maven.plugins.mojos.KuberamAbstractMojo;

/**
 * Generates the HTML index for a set of EXPath specifications. The index will
 * but generated in an output directory that has to be specified. <br>
 *
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius
 * Teodorescu</a>
 */

@Mojo(name = "generate-specs-index")
public class GenerateSpecsIndexMojo extends KuberamAbstractMojo {

    /**
     * A list of <code>fileSet</code> rules to select the EXPath specifications
     * to generate the index for.
     *
     * @since 0.4.7
     */
    @Parameter
    private FileSet[] filesets;

    /**
     * The directory where the index file will be saved.
     *
     * @since 0.4.7
     */
    @Parameter(required = true)
    private File outputDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        final FileSetManager fileSetManager = new FileSetManager();
        final List<Path> specPaths = new ArrayList<>();

        final Path outputDirPath = outputDir.toPath().toAbsolutePath();
        final Path outputFilePath = outputDirPath.resolve("index.html");

        for (final FileSet fileSet : filesets) {
            final String directory = fileSet.getDirectory();
            final String[] includedFiles = fileSetManager.getIncludedFiles(fileSet);

            for (final String includedFile : includedFiles) {
                final Path includedFilePath = Paths.get(directory, includedFile).normalize();

                if (Files.isDirectory(includedFilePath)) {
                    continue;
                }

                specPaths.add(includedFilePath);
            }
        }
        getLog().debug("specPaths: " + specPaths);

        try {
            Files.createDirectories(outputDirPath);

            final Processor processor = new Processor(true);

            final XQueryCompiler xqueryCompiler = processor.newXQueryCompiler();
            xqueryCompiler.setBaseURI(getProject().getBasedir().toURI());

            final XQueryExecutable xqueryExecutable;
            try(final InputStream is = getClass().getResourceAsStream("generate-specs-index.xql")) {
                xqueryExecutable = xqueryCompiler.compile(is);
            }

            final XQueryEvaluator xqueryEvaluator = xqueryExecutable.load();

            xqueryEvaluator.setExternalVariable(new QName("spec-file-paths"),
                    new XdmAtomicValue(specPaths.stream().map(Object::toString).collect(Collectors.joining(","))));

            final XdmValue result;
            try(final InputStream is = getClass().getResourceAsStream("empty.xml")) {
                xqueryEvaluator.setSource(new SAXSource(new InputSource(is)));
                result = xqueryEvaluator.evaluate();
            }

            final Serializer out = processor.newSerializer();
            out.setOutputProperty(Serializer.Property.METHOD, "xml");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");

            try(final OutputStream os = Files.newOutputStream(outputFilePath)) {
                out.setOutputStream(os);
                processor.writeXdmValue(result, out);
            }
        } catch (final SaxonApiException | IOException e) {
            e.printStackTrace();
        }
    }
}
