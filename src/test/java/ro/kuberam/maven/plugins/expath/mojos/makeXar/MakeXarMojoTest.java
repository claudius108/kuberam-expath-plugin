package ro.kuberam.maven.plugins.expath.mojos.makeXar;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.junit.Test;

import ro.kuberam.maven.plugins.expath.mojos.KuberamAbstractMojoTestBase;
import ro.kuberam.maven.plugins.expath.mojos.MakeXarMojo;
import ro.kuberam.maven.plugins.expath.mojos.StubMavenProject;

public class MakeXarMojoTest extends KuberamAbstractMojoTestBase {

	private Path testDirectory = Paths.get(getBasedir(), "target", "make-xar-tmp");
	private RepositorySystem repositorySystem;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (Files.exists(testDirectory)) {
			Files.walk(testDirectory).map(Path::toFile).sorted((o1, o2) -> -o1.compareTo(o2)).forEach(File::delete);
		}

		repositorySystem = lookup(RepositorySystem.class);
	}

	@Override
	protected void tearDown() throws Exception {
		repositorySystem = null;

		super.tearDown();
	}

	@Test
	public void testApplicationXar() throws Exception {

		Path testPath = Paths.get(getBasedir(), "/src/test/resources/ro/kuberam/maven/plugins/expath/mojos/makeXar");

		MakeXarMojo mojo = this.mojo(testPath.resolve("application-assembly.xml"),
				new File(getBasedir() + File.separator + "target"));

		mojo.execute();
	}

	private MakeXarMojo mojo(Object... properties) throws Exception {
		StubMavenProject mavenProject = new StubMavenProject(new File(getBasedir()));
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

		RepositorySystem repositorySystem = (RepositorySystem) lookup(RepositorySystem.class, "default");
		mojo.setRepoSystem(repositorySystem);

		return mojo;
	}
}
