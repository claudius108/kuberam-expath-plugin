package ro.kuberam.maven.plugins.expath;

/**
 * 
 * A dependencySet allows inclusion and exclusion of project dependencies in the
 * assembly.
 * 
 * 
 * @version $Revision$ $Date$
 */

public class ExpathDependencySet {

	public String groupId;
	public String artifactId;
	public String version;
	public String outputDirectory;
	public String outputFileNameMapping;	

	public String toString() {
		return groupId + ":" + artifactId + ":" + version + " " + outputDirectory;
	}

	public ExpathDependencySet() {

	}

	public ExpathDependencySet(String groupId, String artifactId, String version, String outputDirectory, String outputFileNameMapping) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.outputDirectory = outputDirectory;
		this.outputFileNameMapping = outputFileNameMapping;
	}

}
