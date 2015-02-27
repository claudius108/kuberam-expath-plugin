package ro.kuberam.maven.plugins.expath.mojos;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import ro.kuberam.maven.plugins.mojos.KuberamAbstractMojo;
import ro.kuberam.maven.plugins.mojos.NameValuePair;

/**
 * Transforms an EXPath specification to HTML format. <br/>
 * 
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius
 *         Teodorescu</a>
 * 
 */

@Mojo(name = "transform-spec-to-html")
public class TransformSpecToHtmlMojo extends KuberamAbstractMojo {

	/**
	 * Specification file.
	 * 
	 * @parameter
	 * @since 0.3
	 * 
	 */
	@Parameter(required = true)
	private File specFile;

	/**
	 * Output directory.
	 * 
	 * @parameter
	 * @since 0.2
	 * 
	 */
	@Parameter(required = true)
	private File outputDir;

	/**
	 * Google Analytics account id, in case one needs to track the page.
	 * 
	 * @parameter
	 * @since 0.3
	 * 
	 */
	@Parameter(defaultValue = "")
	private String googleAnalyticsAccountId;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		FileUtils.mkdir(outputDir.getAbsolutePath());

		NameValuePair[] parameters = new NameValuePair[] { new NameValuePair("googleAnalyticsAccountId",
				googleAnalyticsAccountId) };

		xsltTransform(specFile,
				this.getClass().getResource("/ro/kuberam/maven/plugins/expath/xmlspec/transform-spec.xsl")
						.toString(),
				new File(outputDir + File.separator + FileUtils.basename(specFile.getAbsolutePath())
						+ "html").getAbsolutePath(), parameters);
	}

}
