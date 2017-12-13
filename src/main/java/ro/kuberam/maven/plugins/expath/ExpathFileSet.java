package ro.kuberam.maven.plugins.expath;

public class ExpathFileSet extends org.codehaus.plexus.archiver.util.DefaultFileSet {

    public String[] includes;
    public String[] excludes;

    @Override
    public String[] getIncludes() {
        return includes;
    }

    @Override
    public String[] getExcludes() {
        return excludes;
    }

    public void setIncludes(final String includesString) {
        includes = includesString.split(",");
    }

    public void setExcludes(final String excludesString) {
        excludes = excludesString.split(",");
    }
}
