package ro.kuberam.maven.plugins.expath.mojos;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystemSession;

import ro.kuberam.maven.plugins.expath.Utils;

/**
 * Transforms an EXPath specification to HTML format. <br>
 *
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius
 *         Teodorescu</a>
 */

@Mojo(name = "transform-spec-to-html")
public class TransformSpecToHtmlMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	/**
	 * The current repository/network configuration of Maven.
	 */
	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	protected RepositorySystemSession repoSession;

	/**
	 * Specification file.
	 *
	 * @parameter
	 * @since 0.3
	 */
	@Parameter(required = true)
	private File specFile;

	/**
	 * Output directory.
	 *
	 * @parameter
	 * @since 0.2
	 */
	@Parameter(required = true)
	private File outputDir;

	/**
	 * Google Analytics account id, in case one needs to track the page.
	 *
	 * @parameter
	 * @since 0.3
	 */
	@Parameter(defaultValue = "")
	private String googleAnalyticsAccountId;

	public void setProject(final MavenProject project) {
		this.project = project;
	}

	public void setRepoSession(final RepositorySystemSession repoSession) {
		this.repoSession = repoSession;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		FileUtils.mkdir(outputDir.getAbsolutePath());

		final Map<String, String> parameters = new HashMap<>();
		parameters.put("googleAnalyticsAccountId", googleAnalyticsAccountId);

		Utils.xsltTransform(specFile,
				this.getClass().getResource("/ro/kuberam/maven/plugins/expath/xmlspec/transform-spec.xsl").toString(),
				new File(outputDir + File.separator + FileUtils.basename(specFile.getAbsolutePath()) + "html")
						.getAbsolutePath(),
				parameters);
	}

}
