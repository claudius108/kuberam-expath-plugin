package ro.kuberam.maven.plugins.expath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DescriptorConfiguration extends Xpp3Dom {

    public DescriptorConfiguration(final Xpp3Dom src) {
        super(src);
    }

    /**
     *
     */
    private static final long serialVersionUID = -8323628485538303936L;

    public List<ExpathFileSet> getFileSets() {

        final List<ExpathFileSet> fileSets = new ArrayList<>();
        final Xpp3Dom fileSetsElement = this.getChild("fileSets");
        if (null != fileSetsElement) {
            final Xpp3Dom[] fileSetChildren = fileSetsElement.getChildren("fileSet");
            for (final Xpp3Dom fileSetChild : fileSetChildren) {
                final ExpathFileSet fileSet = new ExpathFileSet();
                fileSet.setDirectory(new File(fileSetChild.getChild("directory").getValue()));

                final String outputDirectory = getOutputDirectory(fileSetChild);

                fileSet.setPrefix(outputDirectory);

                fileSet.setIncludes("**/*.*");
                final Xpp3Dom includesElement = fileSetChild.getChild("includes");
                if (null != includesElement) {
                    final StringBuilder includesString = new StringBuilder();
                    final Xpp3Dom[] includeElements = includesElement.getChildren("include");
                    for (final Xpp3Dom includeElement : includeElements) {
                        includesString.append(includeElement.getValue()).append(',');
                    }
                    fileSet.setIncludes(includesString.substring(0, includesString.length() - 1));
                }
                fileSet.setExcludes(".project/,.settings/");
                final Xpp3Dom excludesElement = fileSetChild.getChild("excludes");
                if (null != excludesElement) {
                    final StringBuilder excludesString = new StringBuilder();
                    final Xpp3Dom[] excludeElements = excludesElement.getChildren("exclude");
                    for (final Xpp3Dom excludeElement : excludeElements) {
                        excludesString.append(excludeElement.getValue()).append(',');
                    }
                    excludesString.append( ".project/,.settings/");
                    fileSet.setExcludes(excludesString.toString());
                }
                fileSets.add(fileSet);
            }
        }

        return fileSets;
    }

    public List<ExpathDependencySet> getDependencySets() {
        final List<ExpathDependencySet> dependencySets = new ArrayList<>();
        final Xpp3Dom dependencySetsElement = this.getChild("dependencySets");
        if (null != dependencySetsElement) {
            final Xpp3Dom[] dependencySetChildren = dependencySetsElement.getChildren("dependencySet");
            for (final Xpp3Dom dependencySetChild : dependencySetChildren) {

                final String outputDirectory = getOutputDirectory(dependencySetChild);
                final Xpp3Dom outputFileNameMappingElement = dependencySetChild.getChild("outputFileNameMapping");
                final String outputFileNameMapping = Optional.ofNullable(outputFileNameMappingElement).map(Xpp3Dom::getValue).orElse("");

                dependencySets.add(new ExpathDependencySet(dependencySetChild.getChild("groupId").getValue(),
                        dependencySetChild.getChild("artifactId").getValue(),
                        dependencySetChild.getChild("version").getValue(), outputDirectory, outputFileNameMapping));
            }
        }

        return dependencySets;
    }

    public List<ExpathXquerySet> getXquerySets() {
        final List<ExpathXquerySet> xquerySets = new ArrayList<>();
        final Xpp3Dom xquerySetsElement = this.getChild("xquerySets");
        if (null != xquerySetsElement) {
            final Xpp3Dom[] xquerySetElementChildren = xquerySetsElement.getChildren("xquerySet");
            for (final Xpp3Dom xquerySetElementChild : xquerySetElementChildren) {
                final ExpathXquerySet xquerySet = new ExpathXquerySet();

                xquerySet.setDirectory(new File(xquerySetElementChild.getChild("directory").getValue()));

                xquerySet.setNamespace(xquerySetElementChild.getChild("namespace").getValue());

                final String outputDirectory = getOutputDirectory(xquerySetElementChild);
                xquerySet.setPrefix(outputDirectory);

                xquerySet.setIncludes("**/*.*");
                final Xpp3Dom includesElement = xquerySetElementChild.getChild("includes");
                if (null != includesElement) {
                    final StringBuilder includesString = new StringBuilder();
                    final Xpp3Dom[] includeElements = includesElement.getChildren("include");
                    for (final Xpp3Dom includeElement : includeElements) {
                        includesString.append(includeElement.getValue()).append(',');
                    }
                    xquerySet.setIncludes(includesString.substring(0, includesString.length() - 1));
                }

                xquerySet.setExcludes(".project/,.settings/");
                final Xpp3Dom excludesElement = xquerySetElementChild.getChild("excludes");
                if (null != excludesElement) {
                    final StringBuilder excludesString = new StringBuilder();
                    final Xpp3Dom[] excludeElements = excludesElement.getChildren("exclude");
                    for (final Xpp3Dom excludeElement : excludeElements) {
                        excludesString.append(excludeElement.getValue()).append(',');
                    }
                    excludesString.append(".project/,.settings/");
                    xquerySet.setExcludes(excludesString.toString());
                }
                xquerySets.add(xquerySet);
            }
        }

        return xquerySets;
    }

    public String getModuleNamespace() {
        final Xpp3Dom moduleNamespaceElement = this.getChild("module-namespace");
        if (null != moduleNamespaceElement) {
            return moduleNamespaceElement.getValue();
        }
        return "";
    }

    public static String getOutputDirectory(final Xpp3Dom parentElement) {
        final Xpp3Dom outputDirectoryElement = parentElement.getChild("outputDirectory");

        String outputDirectory = Optional.ofNullable(outputDirectoryElement).map(Xpp3Dom::getValue).orElse("");
        if (!outputDirectory.endsWith("/")) {
            outputDirectory = outputDirectory + "/";
        }
        outputDirectory = outputDirectory.replaceAll("^/", "");

        return outputDirectory;
    }

}
