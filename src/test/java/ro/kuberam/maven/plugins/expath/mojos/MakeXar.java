package ro.kuberam.maven.plugins.expath.mojos;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Ignore;
import org.junit.Test;

import ro.kuberam.maven.plugins.expath.mojos.MakeXarMojo;

public class MakeXar extends AbstractMojoTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// code
	}

	@Override
	protected void tearDown() throws Exception {
		// code
		super.tearDown();
	}

	@Ignore
	@Test
	public void testMinConfiguration() throws Exception {
		//executeMojo("plugin-config.xml");
	}

	private MakeXarMojo getMojo(String pluginXml) throws Exception {
		return (MakeXarMojo) lookupMojo("make-xar-new", new File(getBasedir()
				+ "/src/test/resources/ro/kuberam/maven/xarPlugin/tests/" + pluginXml));
	}

	private MakeXarMojo executeMojo(final String pluginXml) throws Exception {
		MakeXarMojo mojo = getMojo(pluginXml);

		System.out.println("mojo.getClass(): " + mojo.getClass());

		mojo.execute();

		return mojo;
	}
}
