package ro.kuberam.maven.plugins.expath;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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

}
