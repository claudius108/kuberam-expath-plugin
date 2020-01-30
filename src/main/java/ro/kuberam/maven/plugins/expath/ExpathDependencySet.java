package ro.kuberam.maven.plugins.expath;

/**
 * A dependencySet allows inclusion and exclusion of project dependencies in the
 * assembly.
 *
 * @version $Revision$ $Date$
 */

public class ExpathDependencySet {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String outputDirectory;
    private final String outputFileNameMapping;

    public ExpathDependencySet(final String groupId, final String artifactId, final String version, final String classifier, final String outputDirectory, final String outputFileNameMapping) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.outputDirectory = outputDirectory;
        this.outputFileNameMapping = outputFileNameMapping;
    }

    @Override
    public String toString() {

        return groupId + ":" + artifactId + (classifier != null ? ":jar:" + classifier : "") + ":" + version   + " " + outputDirectory;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() { return classifier; }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public String getOutputFileNameMapping() {
        return outputFileNameMapping;
    }
}
