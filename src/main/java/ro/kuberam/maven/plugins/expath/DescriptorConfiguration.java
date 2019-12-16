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
	private String defaultIncludes = "**/*.*";
	private String defaultExcludes = ".project,.settings/,target/,pom.xml,build/";

	public List<ExpathFileSet> getFileSets() {

		List<ExpathFileSet> sets = new ArrayList<>();
		Xpp3Dom setsElement = this.getChild("fileSets");
		if (null != setsElement) {
			Xpp3Dom[] setElements = setsElement.getChildren("fileSet");
			for (Xpp3Dom setElement : setElements) {
				ExpathFileSet set = new ExpathFileSet();
				set.setDirectory(new File(setElement.getChild("directory").getValue()));

				String outputDirectory = getOutputDirectory(setElement);

				set.setPrefix(outputDirectory);

				setIncludes(set, setElement.getChild("includes"));
				setEXcludes(set, setElement.getChild("excludes"));
				
				sets.add(set);
			}
		}

		return sets;
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
		List<ExpathXquerySet> sets = new ArrayList<>();
		Xpp3Dom setsElement = this.getChild("xquerySets");
		if (null != setsElement) {
			Xpp3Dom[] setElements = setsElement.getChildren("xquerySet");
			for (Xpp3Dom setElement : setElements) {
				ExpathXquerySet set = new ExpathXquerySet();

				set.setDirectory(new File(setElement.getChild("directory").getValue()));

				set.setNamespace(setElement.getChild("namespace").getValue());

				String outputDirectory = getOutputDirectory(setElement);
				set.setPrefix(outputDirectory);

				setIncludes(set, setElement.getChild("includes"));
				setEXcludes(set, setElement.getChild("excludes"));
				
				sets.add(set);
			}
		}

		return sets;
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
	
	private ExpathFileSet setIncludes(ExpathFileSet set, Xpp3Dom includesElement) {
		String includesString = Optional.ofNullable(includesElement).map(e -> {
			Xpp3Dom[] includeElements = includesElement.getChildren("include");
			StringBuilder sb = new StringBuilder();

			for (Xpp3Dom includeElement : includeElements) {
				sb.append(includeElement.getValue()).append(',');
			}

			return sb.toString();
		}).orElse(defaultIncludes);
		set.setIncludes(includesString);
		
		return set;
	}
	
	private ExpathFileSet setEXcludes(ExpathFileSet set, Xpp3Dom excludesElement) {
		String excludesString = Optional.ofNullable(excludesElement).map(e -> {
			Xpp3Dom[] excludeElements = excludesElement.getChildren("exclude");
			StringBuilder sb = new StringBuilder();

			for (Xpp3Dom excludeElement : excludeElements) {
				sb.append(excludeElement.getValue()).append(',');
			}
			sb.append(defaultExcludes);

			return sb.toString();
		}).orElse(defaultExcludes);
		set.setExcludes(excludesString);	
		
		return set;
	}

}
