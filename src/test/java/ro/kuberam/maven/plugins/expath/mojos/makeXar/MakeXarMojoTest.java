package ro.kuberam.maven.plugins.expath.mojos.makeXar;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.apache.maven.repository.RepositorySystem;
import org.junit.Test;

import ro.kuberam.maven.plugins.expath.mojos.KuberamAbstractMojoTestBase;
import ro.kuberam.maven.plugins.expath.mojos.MakeXarMojo;
import ro.kuberam.maven.plugins.expath.mojos.StubMavenProject;

public class MakeXarMojoTest extends KuberamAbstractMojoTestBase {

	private Path testDirectory = Paths.get(getBasedir(), "target", "make-xar-tmp");
	private RepositorySystem repositorySystem;

	protected void setUp() throws Exception {
		super.setUp();

		if (Files.exists(testDirectory)) {
			Files.walk(testDirectory).map(Path::toFile).sorted((o1, o2) -> -o1.compareTo(o2)).forEach(File::delete);
		}
		repositorySystem = lookup(RepositorySystem.class);
	}

	protected void tearDown() throws Exception {
		repositorySystem = null;

		super.tearDown();
	}

	@Test
	public void testApplicationXar() throws Exception {

		Path testPath = Paths.get(baseDir, "/src/test/resources/ro/kuberam/maven/plugins/expath/mojos/makeXar");

		MakeXarMojo mojo = this.mojo2(testPath.resolve("application-assembly.xml"), new File(projectBuildDirectory));

		mojo.execute();
	}

	private MakeXarMojo mojo2(Object... properties) throws ComponentLookupException, IllegalAccessException {
		StubMavenProject mavenProject = new StubMavenProject(new File(baseDir));
		mavenProject.setVersion("1.0");
		mavenProject.setGroupId("ro.kuberam");
		mavenProject.setName("test project");

		MakeXarMojo mojo = new MakeXarMojo();
		setVariableValueToObject(mojo, "descriptor", ((Path) properties[0]).toFile());
		setVariableValueToObject(mojo, "outputDir", (File) properties[1]);
		setVariableValueToObject(mojo, "projectBuildDirectory", new File(projectBuildDirectory));
		mojo.setProject(mavenProject);
		mojo.setMavenResourcesFiltering(lookup(MavenResourcesFiltering.class));
		ZipArchiver zipArchiver = (ZipArchiver) lookup(Archiver.ROLE, "zip");
		mojo.setZipArchiver(zipArchiver);

		RepositorySystem repositorySystem = (RepositorySystem) lookup(
				org.apache.maven.repository.RepositorySystem.class);
		System.out.println(repositorySystem);
		// mojo.setRepoSystem(lookup(RepositorySystem.class));

		return mojo;
	}
	//
	// private MakeXarMojo mojo() throws Exception {
	// MakeXarMojo mojo = new MakeXarMojo();
	// setVariableValueToObject(mojo, "projectBuildDirectory", new
	// File(projectBuildDirectory));
	// mojo.setProject(mock(MavenProject.class));
	// MavenProject project = mojo.getProject();
	// mojo.setMavenResourcesFiltering(mock(MavenResourcesFiltering.class));
	// mojo.setSession(mock(MavenSession.class));
	// mojo.setRepoSession(newSession(newRepositorySystem()));
	//
	// return mojo;
	// }

}
