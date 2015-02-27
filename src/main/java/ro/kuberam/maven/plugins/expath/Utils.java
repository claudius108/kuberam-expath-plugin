package ro.kuberam.maven.plugins.expath;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class Utils {

	public static String getOutputDirectory(Xpp3Dom parentElement) {
		Xpp3Dom outputDirectoryElement = parentElement.getChild("outputDirectory");

		String outputDirectory = "";
		if (null != outputDirectoryElement) {
			outputDirectory = outputDirectoryElement.getValue();
		} else {
			
		}

		if (!outputDirectory.endsWith("/")) {
			outputDirectory = outputDirectory + "/";
		}

		outputDirectory = outputDirectory.replaceAll("^/", "");
		
		return outputDirectory;
	}

}
