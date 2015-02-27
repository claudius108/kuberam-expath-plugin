package ro.kuberam.maven.plugins.expath.mojos;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

import ro.kuberam.maven.plugins.mojos.KuberamAbstractMojo;
import ro.kuberam.maven.plugins.mojos.NameValuePair;

/**
 * Generates the HTML index for a directory containing EXPath specifications.
 * The index will but generated in the directory containing the specifications. <br/>
 * 
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius
 *         Teodorescu</a>
 * 
 */

@Mojo(name = "generate-specs-index")
public class GenerateSpecsIndexMojo extends KuberamAbstractMojo {

	/**
	 * Directory containing the specifications.
	 * 
	 * @parameter
	 * @since 0.2
	 * 
	 */
	@Parameter(required = true)
	private File specsDir;

	/**
	 * List of specification files' basenames.
	 * 
	 * @parameter
	 * @since 0.2
	 * 
	 */
	@Parameter(required = true)
	private String includeSpecs;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		String specsDirPath = specsDir.getAbsolutePath();
		String specsIndexTmpDir = projectBuildDirectory.getAbsolutePath() + File.separator
				+ "specs-index-tmp-" + UUID.randomUUID();

		// create a copy of the XSL file used for generation of index file
		File xslFile = new File(specsIndexTmpDir + File.separator + "generate-specs-index.xsl");
		try {
			FileUtils.copyStreamToFile(
					new RawInputStreamFacade(this.getClass().getResourceAsStream(
							"/ro/kuberam/maven/plugins/expath/generate-specs-index.xsl")), xslFile);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		NameValuePair[] parameters = new NameValuePair[] {
				new NameValuePair("specsDir", specsDir.getAbsolutePath()),
				new NameValuePair("includeSpecs", includeSpecs),
				new NameValuePair("outputDir", specsDirPath) };

		xsltTransform(xslFile,
				this.getClass().getResource("/ro/kuberam/maven/plugins/expath/generate-specs-index.xsl")
						.toString(),
				new File(specsDirPath + File.separator + "index.html").getAbsolutePath(), parameters);
	}

}
