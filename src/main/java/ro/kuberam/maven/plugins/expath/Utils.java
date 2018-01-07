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

	public static void xsltTransform(final File sourceFile, final String xsltUrl, final String resultDir, final Map<String, String> parameters)
			throws MojoExecutionException, MojoFailureException {
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

		final TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			final Transformer transformer = tFactory.newTransformer(new StreamSource(xsltUrl));

			for (final Entry<String, String> parameter : parameters.entrySet()) {
				transformer.setParameter(parameter.getKey(), parameter.getValue());
			}

			transformer.transform(new StreamSource(sourceFile), new StreamResult(new File(resultDir)));
		} catch (final TransformerException ex) {
			throw new MojoFailureException("An error occurred whilst transforming: " + sourceFile.getAbsolutePath()
					+ " with: " + xsltUrl + " using parameters [" + parameters + "]", ex);
		}
	}

	public static void filterResource(final MavenProject project, final MavenSession session,
                                      final MavenResourcesFiltering mavenResourcesFiltering, final String encoding, final String directory, final String include,
                                      final String targetPath, final File outputDirectory) {
		final List<String> filters = Collections.emptyList();
		final List<String> defaultNonFilteredFileExtensions = Arrays.asList("jpg", "jpeg", "gif", "bmp", "png");

		final Resource resource = new Resource();
		resource.setDirectory(directory);
		resource.addInclude(include);
		resource.setFiltering(true);
		resource.setTargetPath(targetPath);

		final MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(
				Collections.singletonList(resource), outputDirectory, project, encoding, filters,
				defaultNonFilteredFileExtensions, session);
		System.out.println("mavenResourcesExecution: " + mavenResourcesExecution);
		mavenResourcesExecution.setInjectProjectBuildFilters(false);
		mavenResourcesExecution.setOverwrite(true);
		mavenResourcesExecution.setSupportMultiLineFiltering(true);

		try {
			mavenResourcesFiltering.filterResources(mavenResourcesExecution);
		} catch (final MavenFilteringException e) {
			e.printStackTrace();
		}

	}

}
