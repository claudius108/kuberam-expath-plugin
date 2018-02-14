package ro.kuberam.maven.plugins.expath.mojos;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ro.kuberam.maven.plugins.expath.PackageConstants.COMPONENTS_FILENAME;
import static ro.kuberam.maven.plugins.expath.PackageConstants.CONTENTS_ELEM_NAME;
import static ro.kuberam.maven.plugins.expath.PackageConstants.EXPATH_PKG_MODULE_MAIN_CLASS_NS;
import static ro.kuberam.maven.plugins.expath.PackageConstants.EXPATH_PKG_MODULE_NAMESPACE_NS;
import static ro.kuberam.maven.plugins.expath.PackageConstants.FILE_ELEM_NAME;
import static ro.kuberam.maven.plugins.expath.PackageConstants.NAMESPACE_ELEM_NAME;
import static ro.kuberam.maven.plugins.expath.PackageConstants.PACKAGE_ELEM_NAME;
import static ro.kuberam.maven.plugins.expath.PackageConstants.PUBLIC_URI_ELEM_NAME;
import static ro.kuberam.maven.plugins.expath.PackageConstants.RESOURCE_ELEM_NAME;
import static ro.kuberam.maven.plugins.expath.PackageConstants.XQUERY_ELEM_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;

import javax.xml.transform.stream.StreamSource;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import ro.kuberam.maven.plugins.expath.DescriptorConfiguration;
import ro.kuberam.maven.plugins.expath.ExpathDependencySet;
import ro.kuberam.maven.plugins.expath.ExpathFileSet;
import ro.kuberam.maven.plugins.expath.ExpathXquerySet;
import ro.kuberam.maven.plugins.expath.Utils;
import ro.kuberam.maven.plugins.expath.XmlStringBuilder;

/**
 * Assembles a package. <br>
 *
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius
 *         Teodorescu</a>
 */

@Mojo(name = "make-xar", defaultPhase = LifecyclePhase.PACKAGE)
public class MakeXarMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;

	@Component(role = MavenResourcesFiltering.class, hint = "default")
	private MavenResourcesFiltering mavenResourcesFiltering;

	/**
	 * The output directory of the assembled distribution file.
	 */
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File projectBuildDirectory;

	/**
	 * The character encoding scheme to be applied when filtering resources.
	 */
	@Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}", readonly = true)
	private String encoding;

	/**
	 * The filename of the assembled distribution file.
	 */
	@Parameter(defaultValue = "${project.build.finalName}", required = true)
	private String finalName;

	@Parameter(required = true)
	private File descriptor;

	@Parameter(defaultValue = "${project.build.directory}")
	private File outputDir;

	/**
	 * The project's remote repositories to use for the resolution of project
	 * dependencies.
	 */
	@Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
	private List<ArtifactRepository> artifactRepositories;

	@Component(role = org.codehaus.plexus.archiver.Archiver.class, hint = "zip")
	private ZipArchiver zipArchiver;

	@Component
	private RepositorySystem repoSystem;

	/**
	 * The current repository/network configuration of Maven.
	 * 
	 * @parameter default-value="${repositorySystemSession}"
	 * @readonly
	 */
	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;

	/**
	 * The project's remote repositories to use for the resolution.
	 * 
	 * @parameter default-value="${project.remoteProjectRepositories}"
	 * @readonly
	 */
	private List<RemoteRepository> remoteRepos;

	private static final String componentsTemplateFileContent = new XmlStringBuilder().startDocument()
			.xmlDeclaration("1.0", UTF_8).startElement(PACKAGE_ELEM_NAME).text("${components}")
			.endElement(PACKAGE_ELEM_NAME).endDocument().build();

	public void setProject(final MavenProject project) {
		this.project = project;
	}

	public MavenProject getProject() {
		return project;
	}

	public void setMavenResourcesFiltering(final MavenResourcesFiltering mavenResourcesFiltering) {
		this.mavenResourcesFiltering = mavenResourcesFiltering;
	}

	public void setSession(MavenSession session) {
		this.session = session;
	}

	public void setZipArchiver(ZipArchiver zipArchiver) {
		this.zipArchiver = zipArchiver;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {

		// test if descriptor file exists
		if (!descriptor.exists()) {
			throw new MojoExecutionException(
					"Global descriptor file '" + descriptor.getAbsolutePath() + "' does not exist.");
		}

		// set needed variables
		encoding = Optional.ofNullable(encoding).orElse("UTF-8");
		final String outputDirectoryPath = outputDir.getAbsolutePath();
		getLog().info("outputDirectoryPath = " + outputDirectoryPath);
		final String assemblyDescriptorName = descriptor.getName();
		final String archiveTmpDirectoryPath = projectBuildDirectory + File.separator + "make-xar-tmp";
		final Path descriptorsDirectoryPath = Paths.get(outputDirectoryPath, "expath-descriptors");
		getLog().info("descriptorsDirectoryPath: " + descriptorsDirectoryPath);

		// filter the descriptor file
		Utils.filterResource(project, session, mavenResourcesFiltering, encoding, descriptor.getParent(),
				assemblyDescriptorName, archiveTmpDirectoryPath, outputDir);
		final File filteredDescriptor = Paths.get(archiveTmpDirectoryPath, assemblyDescriptorName).toFile();

		// get the execution configuration
		final DescriptorConfiguration executionConfig;
		try (final Reader fileReader = new FileReader(filteredDescriptor)) {
			executionConfig = new DescriptorConfiguration(Xpp3DomBuilder.build(fileReader));
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}

		// extract settings from execution configuration
		final List<ExpathFileSet> fileSets = executionConfig.getFileSets();
		final List<ExpathDependencySet> dependencySets = executionConfig.getDependencySets();
		final List<ExpathXquerySet> xquerySets = executionConfig.getXquerySets();
		final String moduleNamespace = executionConfig.getModuleNamespace();

		// set the zip archiver
		zipArchiver.setCompress(true);
		zipArchiver.setDestFile(Paths.get(outputDirectoryPath, finalName + ".xar").toFile());
		zipArchiver.setForced(true);

		Path existComponents = Paths.get(descriptorsDirectoryPath.toAbsolutePath().toString(), "exist-components.xml");
		try {
			Files.createDirectories(descriptorsDirectoryPath);

			Utils.xqueryTransformation(new FileInputStream(filteredDescriptor),
					getClass().getResourceAsStream("generate-descriptors.xql"), project.getBasedir().toURI(), null,
					existComponents);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage());
		}

		final XmlStringBuilder components = new XmlStringBuilder().startDocument().startElement(CONTENTS_ELEM_NAME);

		// process the maven type dependencies
		for (int i = 0, il = dependencySets.size(); i < il; i++) {
			final ExpathDependencySet dependencySet = dependencySets.get(i);
			String dependencySetOutputDirectory = dependencySet.getOutputDirectory();
			final String outputFileNameMapping = dependencySet.getOutputFileNameMapping();

			Artifact artifact;
			try {
				artifact = new DefaultArtifact(dependencySet.getGroupId() + ":" + dependencySet.getArtifactId() + ":"
						+ dependencySet.getVersion());
			} catch (IllegalArgumentException e) {
				throw new MojoFailureException(e.getMessage(), e);
			}

			ArtifactRequest request = new ArtifactRequest();
			request.setArtifact(artifact);
			request.setRepositories(remoteRepos);
			getLog().info("Resolving artifact " + artifact + ".");

			ArtifactResult result;
			File artifactFile;
			try {
				result = repoSystem.resolveArtifact(repoSession, request);
				artifactFile = result.getArtifact().getFile();
			} catch (ArtifactResolutionException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
			getLog().info("Resolved artifact " + artifact + " to " + artifactFile + " from " + result.getRepository());

			String artifactFileAbsolutePath = artifactFile.getAbsolutePath();
			String artifactFileName = artifactFile.getName();
			if (!outputFileNameMapping.isEmpty()) {
				artifactFileName = outputFileNameMapping;
			}
			String archiveComponentPath = artifactFileName;
			getLog().debug("archiveComponentPath: " + archiveComponentPath);

			dependencySetOutputDirectory = dependencySetOutputDirectory + artifactFileName;

			// add file to archive
			if (artifactFileAbsolutePath.endsWith(".jar")) {
				archiveComponentPath = "content/" + archiveComponentPath;
			}
			zipArchiver.addFile(artifactFile, archiveComponentPath);
			getLog().debug("archiveComponentPath: " + archiveComponentPath);
		}

		// process the xquery sets

		for (final ExpathXquerySet xquerySet : xquerySets) {
			zipArchiver.addFileSet(xquerySet);

			final String namespace = xquerySet.getNamespace();

			for (final String include : xquerySet.getIncludes()) {
				components.startElement(XQUERY_ELEM_NAME).startElement(NAMESPACE_ELEM_NAME).text(namespace)
						.endElement(NAMESPACE_ELEM_NAME)

						.startElement(FILE_ELEM_NAME).text(include).endElement(FILE_ELEM_NAME)
						.endElement(XQUERY_ELEM_NAME);
			}
		}

		// process the file sets
		for (final ExpathFileSet fileSet : fileSets) {
			zipArchiver.addFileSet(fileSet);
		}

		// collect metadata about the archive's entries
		final ResourceIterator itr = zipArchiver.getResources();
		while (itr.hasNext()) {
			final ArchiveEntry entry = itr.next();
			final String entryPath = entry.getName();

			// resource files
			if (entryPath.endsWith(".jar")) {
				components.startElement(RESOURCE_ELEM_NAME).startElement(PUBLIC_URI_ELEM_NAME).text(moduleNamespace)
						.endElement(PUBLIC_URI_ELEM_NAME)

						.startElement(FILE_ELEM_NAME).text(entryPath).endElement(FILE_ELEM_NAME)
						.endElement(RESOURCE_ELEM_NAME);
			}
		}

		String componentsString = unwrap(components.endElement(CONTENTS_ELEM_NAME).endDocument().build());

		project.getModel().addProperty("components", componentsString);

		// create and filter the components descriptor
		final File componentsTemplateFile = Paths.get(archiveTmpDirectoryPath, COMPONENTS_FILENAME).toFile();
		try {
			FileUtils.fileWrite(componentsTemplateFile, UTF_8.displayName(), componentsTemplateFileContent);
		} catch (final IOException e2) {
			e2.printStackTrace();
		}
		Utils.filterResource(project, session, mavenResourcesFiltering, encoding, archiveTmpDirectoryPath,
				"components.xml", descriptorsDirectoryPath.toString(), outputDir);

		// generate the expath descriptors
		try {
			final Processor proc = new Processor(false);
			final XsltCompiler comp = proc.newXsltCompiler();
			final XsltExecutable exp = comp.compile(new StreamSource(this.getClass()
					.getResource("/ro/kuberam/maven/plugins/expath/generate-descriptors.xsl").toString()));
			final XdmNode source = proc.newDocumentBuilder().build(new StreamSource(filteredDescriptor));
			final Serializer out = proc.newSerializer();
			out.setOutputProperty(Serializer.Property.METHOD, "xml");
			out.setOutputProperty(Serializer.Property.INDENT, "yes");
			out.setOutputFile(new File("output.xml"));
			final XsltTransformer transformer = exp.load();
			transformer.setInitialContextNode(source);
			transformer.setDestination(out);
			transformer.setBaseOutputURI(descriptorsDirectoryPath.toUri().toString());

			transformer.setParameter(new net.sf.saxon.s9api.QName("package-dir"),
					new XdmAtomicValue(descriptorsDirectoryPath.toUri()));

			transformer.transform();

			Files.delete(descriptorsDirectoryPath.resolve("components.xml"));
			Files.delete(existComponents);
		} catch (final SaxonApiException | IOException e) {
			e.printStackTrace();
		}
		// add the expath descriptors
		// File descriptorsDirectory = descriptorsDirectoryPath.toFile();
		// try {
		// Files.list(descriptorsDirectoryPath).forEach(zipArchiver::addFile);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(descriptorsDirectoryPath)) {
			for (final Path entry : stream) {
				zipArchiver.addFile(entry.toFile(), entry.getFileName().toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			zipArchiver.createArchive();
		} catch (ArchiverException | IOException e1) {
			e1.printStackTrace();
		}

		project.getModel().addProperty("components", "");
	}

	/**
	 * just removes the outer container element of some xml
	 */
	private String unwrap(final String xml) {
		if (xml == null) {
			return null;
		}

		return xml.replaceAll("^<[^>]+>(.*)", "$1").replaceAll("(.*)</[^>]+>$", "$1");
	}

	private static List<String> getMainClass(final String firstDependencyAbsolutePath) {
		final List<String> result = new ArrayList<>();

		Attributes attr = null;
		try {
			final URL u = new URL("jar", "", "file://" + firstDependencyAbsolutePath + "!/");
			final JarURLConnection uc = (JarURLConnection) u.openConnection();
			attr = uc.getMainAttributes();
		} catch (final Exception e1) {
			e1.printStackTrace();
		}

		result.add(attr.getValue(Attributes.Name.MAIN_CLASS));
		result.add(attr.getValue("ModuleNamespace"));

		return result;
	}

}
