package ro.kuberam.maven.plugins.expath.mojos.transformSpecToHtml;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

import ro.kuberam.maven.plugins.expath.mojos.TransformSpecToHtmlMojo;
import ro.kuberam.maven.plugins.mojos.KuberamAbstractMojoTestBase;

public class TransformSpecToHtmlMojoTest extends KuberamAbstractMojoTestBase {

	@Test
	public void testMojoExecute() throws Exception {
		final TransformSpecToHtmlMojo mojo = this.mojo();
		setVariableValueToObject(mojo, "specFile", new File(baseDir
				+ "/src/test/resources/ro/kuberam/maven/plugins/expath/mojos/transformSpecToHtml/crypto.xml"));
		setVariableValueToObject(mojo, "outputDir", new File(projectBuildDirectory));
		setVariableValueToObject(mojo, "googleAnalyticsAccountId", "googleAnalyticsAccountId");
		mojo.execute();
	}

	private TransformSpecToHtmlMojo mojo() throws Exception {
		final TransformSpecToHtmlMojo mojo = new TransformSpecToHtmlMojo();
		setVariableValueToObject(mojo, "outputDir", new File(projectBuildDirectory));
		setVariableValueToObject(mojo, "projectBuildDirectory", new File(projectBuildDirectory));
		mojo.setProject(new MavenProject());
		mojo.setRepoSession(newSession(newRepositorySystem()));

		return mojo;
	}

}
