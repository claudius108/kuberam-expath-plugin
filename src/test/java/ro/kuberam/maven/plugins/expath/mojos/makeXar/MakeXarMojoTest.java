package ro.kuberam.maven.plugins.expath.mojos.makeXar;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.junit.Test;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.project.artifact.DefaultMavenMetadataCache.CacheKey;
import org.apache.maven.repository.DelegatingLocalArtifactRepository;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.PlexusTestCase;
import ro.kuberam.maven.plugins.expath.mojos.MakeXarMojo;
import ro.kuberam.maven.plugins.expath.mojos.StubMavenProject;

public class MakeXarMojoTest extends PlexusTestCase {

	private Path testDirectory = Paths.get(getBasedir(), "target", "make-xar-tmp");
	private RepositorySystem repositorySystem;

	private final String LOCAL_REPO = "target/local-repo/";

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

		MakeXarMojo mojo = this.mojo2(testPath.resolve("application-assembly.xml"),
				new File(getBasedir() + File.separator + "target"));

		mojo.execute();
	}

	private MakeXarMojo mojo2(Object... properties) throws Exception {
		StubMavenProject mavenProject = new StubMavenProject(new File(getBasedir()));
		mavenProject.setVersion("1.0");
		mavenProject.setGroupId("ro.kuberam");
		mavenProject.setName("test project");

		MakeXarMojo mojo = new MakeXarMojo();
		// setVariableValueToObject(mojo, "descriptor", ((Path)
		// properties[0]).toFile());
		// setVariableValueToObject(mojo, "outputDir", (File) properties[1]);
		// setVariableValueToObject(mojo, "projectBuildDirectory", new
		// File(projectBuildDirectory));
		mojo.setProject(mavenProject);
		mojo.setMavenResourcesFiltering(lookup(MavenResourcesFiltering.class));
		ZipArchiver zipArchiver = (ZipArchiver) lookup(Archiver.ROLE, "zip");
		mojo.setZipArchiver(zipArchiver);

		RepositorySystem repositorySystem = (RepositorySystem) lookup(RepositorySystem.class, "default");
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
