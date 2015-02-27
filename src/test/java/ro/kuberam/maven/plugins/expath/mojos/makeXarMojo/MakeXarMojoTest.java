package ro.kuberam.maven.plugins.expath.mojos.makeXarMojo;

import static org.mockito.Mockito.mock;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.junit.Test;

import ro.kuberam.maven.plugins.expath.mojos.MakeXarMojo;
import ro.kuberam.maven.plugins.mojos.KuberamAbstractMojoTestBase;

public class MakeXarMojoTest extends KuberamAbstractMojoTestBase {

	@Test
	public void testApplicationXar() throws Exception {
		
		String a = "/";
		
		System.out.println("result: " + a.replaceAll("^/", ""));
//		final MakeXarMojo mojo = this.mojo();
//		setVariableValueToObject(
//				mojo,
//				"descriptor",
//				new File(
//						baseDir
//								+ "/src/test/resources/ro/kuberam/maven/plugins/expath/mojos/makeXarMojo/application-assembly.xml"));
//		setVariableValueToObject(mojo, "outputDir", new File(projectBuildDirectory));
//		mojo.execute();
	}

	private MakeXarMojo mojo() throws Exception {
		final MakeXarMojo mojo = new MakeXarMojo();
		setVariableValueToObject(mojo, "projectBuildDirectory", new File(projectBuildDirectory));
		mojo.setProject(mock(MavenProject.class));
		MavenProject project = mojo.getProject();
		mojo.setMavenResourcesFiltering(mock(MavenResourcesFiltering.class));
		mojo.setSession(mock(MavenSession.class));
		mojo.setRepoSession(newSession(newRepositorySystem()));

		return mojo;
	}

}
