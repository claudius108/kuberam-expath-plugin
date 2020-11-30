package ro.kuberam.maven.plugins.expath.mojos;

import java.io.File;
import java.util.Properties;

import org.apache.maven.project.MavenProject;

public class StubMavenProject extends MavenProject {
	private Properties properties;

	private File basedir;

	public StubMavenProject(File basedir) {
		this.basedir = basedir;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void addProperty(String key, String value) {
		if (this.properties == null) {
			this.properties = new Properties();
		}
		this.properties.put(key, value);
	}

	public File getBasedir() {
		return basedir;
	}

}