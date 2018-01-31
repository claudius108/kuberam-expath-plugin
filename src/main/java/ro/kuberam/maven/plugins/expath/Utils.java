package ro.kuberam.maven.plugins.expath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
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
import org.xml.sax.InputSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;

public class Utils {

	public static void xsltTransform(final File sourceFile, final String xsltUrl, final String resultDir,
			final Map<String, String> parameters) throws MojoExecutionException, MojoFailureException {
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

		final TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			final Transformer transformer = tFactory.newTransformer(new StreamSource(xsltUrl));

			for (final Entry<String, String> parameter : parameters.entrySet()) {
				transformer.setParameter(parameter.getKey(), parameter.getValue());
			}

			transformer.transform(new StreamSource(sourceFile), new StreamResult(new File(resultDir)));
		} catch (final TransformerException e) {
			throw new MojoFailureException("An error occurred whilst transforming: " + sourceFile.getAbsolutePath()
					+ " with: " + xsltUrl + " using parameters [" + parameters + "]", e);
		}
	}

	public static void xqueryTransformation(InputStream xml, InputStream xquery, URI baseURI,
			Map<QName, XdmAtomicValue> parameters, Path outputFilePath) throws MojoFailureException {
		try (InputStream xmlIs = xml;
				InputStream xqueryIs = xquery;
				OutputStream os = Files.newOutputStream(outputFilePath)) {
			Processor processor = new Processor(true);

			XQueryCompiler xqueryCompiler = processor.newXQueryCompiler();
			xqueryCompiler.setBaseURI(baseURI);

			XQueryExecutable xqueryExecutable = xqueryCompiler.compile(xqueryIs);

			XQueryEvaluator xqueryEvaluator = xqueryExecutable.load();

			Optional.ofNullable(parameters).ifPresent(p -> {
				for (Entry<QName, XdmAtomicValue> parameter : p.entrySet()) {
					xqueryEvaluator.setExternalVariable((QName) parameter.getKey(),
							(XdmAtomicValue) parameter.getValue());
				}
			});

			XdmValue result;
			xqueryEvaluator.setSource(new SAXSource(new InputSource(xmlIs)));
			result = xqueryEvaluator.evaluate();

			Serializer out = processor.newSerializer();
			out.setOutputProperty(Serializer.Property.METHOD, "xml");
			out.setOutputProperty(Serializer.Property.INDENT, "yes");
			out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");

			out.setOutputStream(os);
			processor.writeXdmValue(result, out);
		} catch (SaxonApiException | IOException e) {
			throw new MojoFailureException("An error occurred whilst doing an xquery transformation: ", e);
		}

	}

	public static void filterResource(MavenProject project, MavenSession session,
			MavenResourcesFiltering mavenResourcesFiltering, String encoding, String directory, String include,
			String targetPath, File outputDirectory) throws MojoFailureException {
		List<String> filters = Collections.emptyList();
		List<String> defaultNonFilteredFileExtensions = Arrays.asList("jpg", "jpeg", "gif", "bmp", "png");

		Resource resource = new Resource();
		resource.setDirectory(directory);
		resource.addInclude(include);
		resource.setFiltering(true);
		resource.setTargetPath(targetPath);

		MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(
				Collections.singletonList(resource), outputDirectory, project, encoding, filters,
				defaultNonFilteredFileExtensions, session);

		mavenResourcesExecution.setInjectProjectBuildFilters(false);
		mavenResourcesExecution.setOverwrite(true);
		mavenResourcesExecution.setSupportMultiLineFiltering(true);

		try {
			mavenResourcesFiltering.filterResources(mavenResourcesExecution);
		} catch (MavenFilteringException e) {
			throw new MojoFailureException(e.getMessage());
		}

	}

}
