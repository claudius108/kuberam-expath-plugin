package ro.kuberam.maven.plugins.expath;

public class DefaultFileSet extends org.codehaus.plexus.archiver.util.DefaultFileSet {

	public String[] includes;
	public String[] excludes;

	public String[] getIncludes() {
		return includes;
	}

	public String[] getExcludes() {
		return excludes;
	}

	public void setIncludes(String includesString) {
		includes = includesString.split(",");
	}

	public void setExcludes(String excludesString) {
		excludes = excludesString.split(",");
	}
}
