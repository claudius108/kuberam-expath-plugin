package ro.kuberam.maven.plugins.expath.mojos;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.Attributes;

import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import ro.kuberam.maven.plugins.expath.ExpathFileSet;
import ro.kuberam.maven.plugins.expath.ExpathXquerySet;
import ro.kuberam.maven.plugins.expath.ExpathDependencySet;
import ro.kuberam.maven.plugins.expath.DescriptorConfiguration;
import ro.kuberam.maven.plugins.mojos.KuberamAbstractMojo;
import ro.kuberam.maven.plugins.mojos.NameValuePair;

/**
 * Assembles a package. <br>
 *
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius
 * Teodorescu</a>
 */

@Mojo(name = "make-xar")
public class MakeXarMojo extends KuberamAbstractMojo {

    @Parameter(required = true)
    private File descriptor;

    @Parameter(defaultValue = "${project.build.directory}")
    private File outputDir;

    @Component(role = org.codehaus.plexus.archiver.Archiver.class, hint = "zip")
    private ZipArchiver zipArchiver;

    @Component
    private RepositorySystem repoSystem;

    private static String componentsTemplateFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<package xmlns=\"http://exist-db.org/ns/expath-pkg\">${components}</package>";

    public void execute() throws MojoExecutionException, MojoFailureException {

        // test if descriptor file exists
        if (!descriptor.exists()) {
            throw new MojoExecutionException(
                    "Global descriptor file '" + descriptor.getAbsolutePath() + "' does not exist.");
        }

        // set needed variables
        final String outputDirectoryPath = outputDir.getAbsolutePath();
        final String assemblyDescriptorName = descriptor.getName();
        final String archiveTmpDirectoryPath = projectBuildDirectory + File.separator + "make-xar-tmp";
        String components = "";
        final Path descriptorsDirectoryPath = Paths.get(outputDirectoryPath, "expath-descriptors-" + UUID.randomUUID());
        getLog().info("descriptorsDirectoryPath: " + descriptorsDirectoryPath);

        // Plugin xarPlugin =
        // project.getPlugin("ro.kuberam.maven.plugins:kuberam-xar-plugin");
        // DescriptorConfiguration mainConfig = new
        // DescriptorConfiguration((Xpp3Dom) xarPlugin.getConfiguration());

        // filter the descriptor file
        filterResource(descriptor.getParent(), assemblyDescriptorName, archiveTmpDirectoryPath, outputDir);
        final File filteredDescriptor = Paths.get(archiveTmpDirectoryPath, assemblyDescriptorName).toFile();

        // get the execution configuration
        final FileReader fileReader;
        final DescriptorConfiguration executionConfig;
        try {
            fileReader = new FileReader(filteredDescriptor);
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

        // process the maven type dependencies
        for (int i = 0, il = dependencySets.size(); i < il; i++) {
            final ExpathDependencySet dependencySet = dependencySets.get(i);
            String dependencySetOutputDirectory = dependencySet.getOutputDirectory();
            final String outputFileNameMapping = dependencySet.getOutputFileNameMapping();

            // define the artifact
            final Artifact artifactReference;
            try {
                artifactReference = new DefaultArtifact(
                        dependencySet.getGroupId() + ":" + dependencySet.getArtifactId() + ":" + dependencySet.getVersion());
            } catch (final IllegalArgumentException e) {
                throw new MojoFailureException(e.getMessage(), e);
            }

            final String artifactIdentifier = artifactReference.toString();
            getLog().debug("Resolving artifact: " + artifactReference);

            // resolve the artifact
            final ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(artifactReference);
            request.setRepositories(projectRepos);

            final ArtifactResult artifactResult;
            try {
                artifactResult = repoSystem.resolveArtifact(repoSession, request);
            } catch (final ArtifactResolutionException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            getLog().info("Resolved artifact: " + artifactReference);

            final Artifact artifact = artifactResult.getArtifact();
            final File artifactFile = artifact.getFile();
            getLog().info("artifactFile: " + artifactFile);
            final String artifactFileAbsolutePath = artifactFile.getAbsolutePath();
            String artifactFileName = artifactFile.getName();
            if (outputFileNameMapping != "") {
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

            // collect metadata about module's java main class for exist.xml
            if (i == 0 && artifactIdentifier.contains(":jar:")) {
                components += "<resource><public-uri>http://exist-db.org/ns/expath-pkg/module-main-class</public-uri><file>"
                        + getMainClass(artifactFileAbsolutePath).get(0) + "</file></resource>";
                components += "<resource><public-uri>http://exist-db.org/ns/expath-pkg/module-namespace</public-uri><file>"
                        + getMainClass(artifactFileAbsolutePath).get(1) + "</file></resource>";
            }
        }

        // process the xquery sets

        for (final ExpathXquerySet xquerySet : xquerySets) {
            zipArchiver.addFileSet(xquerySet);

            final String namespace = xquerySet.getNamespace();

            for (final String include : xquerySet.getIncludes()) {
                components += "<xquery><namespace>" + namespace + "</namespace><file>" + include + "</file></xquery>";
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
                components += "<resource><public-uri>" + moduleNamespace + "</public-uri><file>" + entryPath
                        + "</file></resource>";
            }
        }

        project.getModel().addProperty("components", components);

        // create and filter the components descriptor
        final File componentsTemplateFile = Paths.get(archiveTmpDirectoryPath, "components.xml").toFile();
        try {
            FileUtils.fileWrite(componentsTemplateFile, "UTF-8", componentsTemplateFileContent);
        } catch (final IOException e2) {
            e2.printStackTrace();
        }
        filterResource(archiveTmpDirectoryPath, "components.xml", descriptorsDirectoryPath.toString(), outputDir);

        // generate the expath descriptors
        final NameValuePair[] parameters = new NameValuePair[]{
                new NameValuePair("package-dir", descriptorsDirectoryPath.toString())};

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
            transformer.setBaseOutputURI(descriptorsDirectoryPath.toString());
            for (final NameValuePair parameter : parameters) {
                transformer.setParameter(new QName(parameter.getName()), new XdmAtomicValue(parameter.getValue()));
            }
            transformer.transform();
        } catch (final SaxonApiException e) {
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
        } catch (final IOException e) {
            e.printStackTrace();
        }

        try {
            zipArchiver.createArchive();
        } catch (final ArchiverException | IOException e1) {
            e1.printStackTrace();
        }

        project.getModel().addProperty("components", "");
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
