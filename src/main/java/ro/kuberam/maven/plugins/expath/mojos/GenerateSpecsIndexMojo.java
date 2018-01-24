package ro.kuberam.maven.plugins.expath.mojos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.FileSet;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import ro.kuberam.maven.plugins.expath.Utils;

/**
 * Generates the HTML index for a set of EXPath specifications. The index will
 * but generated in an output directory that has to be specified. <br>
 *
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius
 *         Teodorescu</a>
 */

@Mojo(name = "generate-specs-index")
public class GenerateSpecsIndexMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	/**
	 * A list of <code>fileSet</code> rules to select the EXPath specifications to
	 * generate the index for.
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

		List<Path> specPaths = new ArrayList<>();

		Path outputDirPath = outputDir.toPath().toAbsolutePath();
		Path outputFilePath = outputDirPath.resolve("index.html");

		for (FileSet fileSet : filesets) {
			String directory = fileSet.getDirectory();
			List<String> includedFiles = fileSet.getIncludes();

			for (String includedFile : includedFiles) {
				Path includedFilePath = Paths.get(directory, includedFile).normalize();

				if (Files.isDirectory(includedFilePath)) {
					continue;
				}

				specPaths.add(includedFilePath);
			}
		}
		getLog().debug("specPaths: " + specPaths);

		InputStream xml = null;
		InputStream xquery = null;
		Map<QName, XdmAtomicValue> parameters = new HashMap<QName, XdmAtomicValue>();
		parameters.put(new QName("spec-file-paths"),
				new XdmAtomicValue(specPaths.stream().map(Object::toString).collect(Collectors.joining(","))));
		URI baseURI = null;

		try {
			xml = getClass().getResourceAsStream("empty.xml");
			xquery = getClass().getResourceAsStream("generate-specs-index.xql");
			baseURI = project.getBasedir().toURI();
			Files.createDirectories(outputDirPath);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage());
		}

		Utils.xqueryTransformation(xml, xquery, baseURI, parameters, outputFilePath);

	}
}
