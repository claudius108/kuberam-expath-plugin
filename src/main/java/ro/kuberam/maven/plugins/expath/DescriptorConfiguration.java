package ro.kuberam.maven.plugins.expath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DescriptorConfiguration extends Xpp3Dom {

	public DescriptorConfiguration(Xpp3Dom src) {
		super(src);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8323628485538303936L;

	public List<DefaultFileSet> getFileSets() {
		List<DefaultFileSet> fileSets = new ArrayList<DefaultFileSet>();
		Xpp3Dom fileSetsElement = this.getChild("fileSets");
		if (null != fileSetsElement) {
			Xpp3Dom[] fileSetChildren = fileSetsElement.getChildren("fileSet");
			for (Xpp3Dom fileSetChild : fileSetChildren) {
				DefaultFileSet fileSet = new DefaultFileSet();
				fileSet.setDirectory(new File(fileSetChild.getChild("directory").getValue()));

				String outputDirectory = Utils.getOutputDirectory(fileSetChild);

				fileSet.setPrefix(outputDirectory);

				fileSet.setIncludes("**/*.*");
				Xpp3Dom includesElement = fileSetChild.getChild("includes");
				if (null != includesElement) {
					String includesString = "";
					Xpp3Dom[] includeElements = includesElement.getChildren("include");
					for (Xpp3Dom includeElement : includeElements) {
						includesString += includeElement.getValue() + ",";
					}
					fileSet.setIncludes(includesString.substring(0, includesString.length() - 1));
				}
				fileSet.setExcludes(".project/,.settings/");
				Xpp3Dom excludesElement = fileSetChild.getChild("excludes");
				if (null != excludesElement) {
					String excludesString = "";
					Xpp3Dom[] excludeElements = excludesElement.getChildren("exclude");
					for (Xpp3Dom excludeElement : excludeElements) {
						excludesString += excludeElement.getValue() + ",";
					}
					fileSet.setExcludes(excludesString + ".project/,.settings/");
				}
				fileSets.add(fileSet);
			}
		}

		return fileSets;
	}

	public List<DependencySet> getDependencySets() {
		List<DependencySet> dependencySets = new ArrayList<DependencySet>();
		Xpp3Dom dependencySetsElement = this.getChild("dependencySets");
		if (null != dependencySetsElement) {
			Xpp3Dom[] dependencySetChildren = dependencySetsElement.getChildren("dependencySet");
			for (Xpp3Dom dependencySetChild : dependencySetChildren) {

				String outputDirectory = Utils.getOutputDirectory(dependencySetChild);

				dependencySets.add(new DependencySet(dependencySetChild.getChild("groupId").getValue(),
						dependencySetChild.getChild("artifactId").getValue(), dependencySetChild.getChild(
								"version").getValue(), outputDirectory));
			}
		}

		return dependencySets;
	}

	public String getModuleNamespace() {
		Xpp3Dom moduleNamespaceElement = this.getChild("module-namespace");
		if (null != moduleNamespaceElement) {
			return moduleNamespaceElement.getValue();
		}
		return "";
	}

}
