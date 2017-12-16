package ro.kuberam.maven.plugins.expath;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

public class Utils {

	public static void xsltTransform(File sourceFile, String xsltUrl, String resultDir, Map<String, String> parameters)
			throws MojoExecutionException, MojoFailureException {
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = tFactory.newTransformer(new StreamSource(xsltUrl));

			for (Entry<String, String> parameter : parameters.entrySet()) {
				transformer.setParameter(parameter.getKey(), parameter.getValue());
			}

			transformer.transform(new StreamSource(sourceFile), new StreamResult(new File(resultDir)));
		} catch (TransformerException ex) {
			throw new MojoFailureException("An error occurred whilst transforming: " + sourceFile.getAbsolutePath()
					+ " with: " + xsltUrl + " using parameters [" + parameters + "]", ex);
		}
	}

	public static void filterResource(MavenProject project, MavenSession session,
			MavenResourcesFiltering mavenResourcesFiltering, String encoding, String directory, String include,
			String targetPath, File outputDirectory) {
		List<String> filters = Arrays.asList();
		List<String> defaultNonFilteredFileExtensions = Arrays.asList("jpg", "jpeg", "gif", "bmp", "png");

		Resource resource = new Resource();
		resource.setDirectory(directory);
		resource.addInclude(include);
		resource.setFiltering(true);
		resource.setTargetPath(targetPath);

		MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(
				Collections.singletonList(resource), outputDirectory, project, encoding, filters,
				defaultNonFilteredFileExtensions, session);
		System.out.println("mavenResourcesExecution: " + mavenResourcesExecution);
		mavenResourcesExecution.setInjectProjectBuildFilters(false);
		mavenResourcesExecution.setOverwrite(true);
		mavenResourcesExecution.setSupportMultiLineFiltering(true);

		try {
			mavenResourcesFiltering.filterResources(mavenResourcesExecution);
		} catch (MavenFilteringException e) {
			e.printStackTrace();
		}

	}

}
