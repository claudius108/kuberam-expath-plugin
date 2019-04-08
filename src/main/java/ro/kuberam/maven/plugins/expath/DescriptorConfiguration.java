package ro.kuberam.maven.plugins.expath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DescriptorConfiguration extends Xpp3Dom {

	public DescriptorConfiguration(Xpp3Dom src) {
		super(src);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -8323628485538303936L;
	private String defaultExcludes = ".project,.settings/,target/,pom.xml";

	public List<ExpathFileSet> getFileSets() {

		List<ExpathFileSet> fileSets = new ArrayList<>();
		Xpp3Dom fileSetsElement = this.getChild("fileSets");
		if (null != fileSetsElement) {
			Xpp3Dom[] fileSetChildren = fileSetsElement.getChildren("fileSet");
			for (Xpp3Dom fileSetChild : fileSetChildren) {
				ExpathFileSet fileSet = new ExpathFileSet();
				fileSet.setDirectory(new File(fileSetChild.getChild("directory").getValue()));

				String outputDirectory = getOutputDirectory(fileSetChild);

				fileSet.setPrefix(outputDirectory);

				fileSet.setIncludes("**/*.*");
				Xpp3Dom includesElement = fileSetChild.getChild("includes");
				if (null != includesElement) {
					StringBuilder includesString = new StringBuilder();
					Xpp3Dom[] includeElements = includesElement.getChildren("include");
					for (Xpp3Dom includeElement : includeElements) {
						includesString.append(includeElement.getValue()).append(',');
					}
					fileSet.setIncludes(includesString.substring(0, includesString.length() - 1));
				}

				Xpp3Dom excludesElement = fileSetChild.getChild("excludes");
				String excludesString = Optional.ofNullable(excludesElement).map(e -> {
					Xpp3Dom[] excludeElements = excludesElement.getChildren("exclude");
					StringBuilder sb = new StringBuilder();

					for (Xpp3Dom excludeElement : excludeElements) {
						sb.append(excludeElement.getValue()).append(',');
					}
					sb.append(defaultExcludes);

					return sb.toString();
				}).orElse(defaultExcludes);
				
				System.out.println("excludesString = " + excludesString);

				fileSet.setExcludes(excludesString);

				fileSets.add(fileSet);
			}
		}

		return fileSets;
	}

	public List<ExpathDependencySet> getDependencySets() {
		List<ExpathDependencySet> dependencySets = new ArrayList<>();
		Xpp3Dom dependencySetsElement = this.getChild("dependencySets");
		if (null != dependencySetsElement) {
			Xpp3Dom[] dependencySetChildren = dependencySetsElement.getChildren("dependencySet");
			for (Xpp3Dom dependencySetChild : dependencySetChildren) {

				String outputDirectory = getOutputDirectory(dependencySetChild);
				Xpp3Dom outputFileNameMappingElement = dependencySetChild.getChild("outputFileNameMapping");
				String outputFileNameMapping = Optional.ofNullable(outputFileNameMappingElement).map(Xpp3Dom::getValue)
						.orElse("");

				dependencySets.add(new ExpathDependencySet(dependencySetChild.getChild("groupId").getValue(),
						dependencySetChild.getChild("artifactId").getValue(),
						dependencySetChild.getChild("version").getValue(), outputDirectory, outputFileNameMapping));
			}
		}

		return dependencySets;
	}

	public List<ExpathXquerySet> getXquerySets() {
		List<ExpathXquerySet> xquerySets = new ArrayList<>();
		Xpp3Dom xquerySetsElement = this.getChild("xquerySets");
		if (null != xquerySetsElement) {
			Xpp3Dom[] xquerySetElementChildren = xquerySetsElement.getChildren("xquerySet");
			for (Xpp3Dom xquerySetElementChild : xquerySetElementChildren) {
				ExpathXquerySet xquerySet = new ExpathXquerySet();

				xquerySet.setDirectory(new File(xquerySetElementChild.getChild("directory").getValue()));

				xquerySet.setNamespace(xquerySetElementChild.getChild("namespace").getValue());

				String outputDirectory = getOutputDirectory(xquerySetElementChild);
				xquerySet.setPrefix(outputDirectory);

				xquerySet.setIncludes("**/*.*");
				Xpp3Dom includesElement = xquerySetElementChild.getChild("includes");
				if (null != includesElement) {
					StringBuilder includesString = new StringBuilder();
					Xpp3Dom[] includeElements = includesElement.getChildren("include");
					for (Xpp3Dom includeElement : includeElements) {
						includesString.append(includeElement.getValue()).append(',');
					}
					xquerySet.setIncludes(includesString.substring(0, includesString.length() - 1));
				}

				xquerySet.setExcludes(".project/,.settings/");
				Xpp3Dom excludesElement = xquerySetElementChild.getChild("excludes");
				if (null != excludesElement) {
					StringBuilder excludesString = new StringBuilder();
					Xpp3Dom[] excludeElements = excludesElement.getChildren("exclude");
					for (Xpp3Dom excludeElement : excludeElements) {
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
		Xpp3Dom moduleNamespaceElement = this.getChild("module-namespace");
		if (null != moduleNamespaceElement) {
			return moduleNamespaceElement.getValue();
		}
		return "";
	}

	public static String getOutputDirectory(Xpp3Dom parentElement) {
		Xpp3Dom outputDirectoryElement = parentElement.getChild("outputDirectory");

		String outputDirectory = Optional.ofNullable(outputDirectoryElement).map(Xpp3Dom::getValue).orElse("/");
		if (!outputDirectory.endsWith("/")) {
			outputDirectory = outputDirectory + "/";
		}
		outputDirectory = outputDirectory.replaceAll("^/", "");

		return outputDirectory;
	}

}
