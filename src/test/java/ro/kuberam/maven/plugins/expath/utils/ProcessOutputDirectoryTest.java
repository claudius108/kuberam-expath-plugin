package ro.kuberam.maven.plugins.expath.utils;

import static org.junit.Assert.assertTrue;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Test;

import ro.kuberam.maven.plugins.expath.Utils;

public class ProcessOutputDirectoryTest {
	
	private Xpp3Dom parentElement;
	private Xpp3Dom outputDirectoryElement;
	
	@Before
    public void initObjects() {
		parentElement = new Xpp3Dom("fileSet");
		outputDirectoryElement = new Xpp3Dom("outputDirectory");
		parentElement.addChild(outputDirectoryElement);
    }

	@Test
	public void testNullOutputDirectory() throws Exception {
		parentElement.removeChild(0);
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals(""));
	}

	@Test
	public void testEmptyOutputDirectory() throws Exception {
		outputDirectoryElement.setValue(""); 		
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals(""));
	}

	@Test
	public void testForwardSlashOutputDirectory() throws Exception {
		outputDirectoryElement.setValue("/"); 
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals(""));
	}

	@Test
	public void testSimpleOutputDirectory1() throws Exception {
		outputDirectoryElement.setValue("a"); 
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals("a/"));
	}

	@Test
	public void testSimpleOutputDirectory2() throws Exception {
		outputDirectoryElement.setValue("/a"); 
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals("a/"));
	}

	@Test
	public void testSimpleOutputDirectory3() throws Exception {
		outputDirectoryElement.setValue("/a/"); 
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals("a/"));
	}

	@Test
	public void testComplexOutputDirectory1() throws Exception {
		outputDirectoryElement.setValue("a/b"); 
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals("a/b/"));
	}
	
	@Test
	public void testComplexOutputDirectory2() throws Exception {
		outputDirectoryElement.setValue("/a/b"); 
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals("a/b/"));
	}
	
	@Test
	public void testComplexOutputDirectory3() throws Exception {
		outputDirectoryElement.setValue("/a/b/"); 
		
		String processedOutputDirectory = Utils.getOutputDirectory(parentElement);
		assertTrue(processedOutputDirectory.equals("a/b/"));
	}	

}
