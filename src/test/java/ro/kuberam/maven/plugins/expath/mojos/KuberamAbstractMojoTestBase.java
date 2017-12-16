package ro.kuberam.maven.plugins.expath.mojos;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.ReflectionUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

public class KuberamAbstractMojoTestBase extends PlexusTestCase {

	protected static String baseDir = PlexusTestCase.getBasedir();
	protected static String projectBuildDirectory = baseDir + File.separator + "target";

	protected static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		return locator.getService(RepositorySystem.class);
	}

	protected static RepositorySystemSession newSession(RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository(projectBuildDirectory + File.separator + "local-repo");
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

		return session;
	}

	protected void setVariableValueToObject(Object object, String variable, Object value) throws IllegalAccessException {
		Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses(variable, object.getClass());
		field.setAccessible(true);
		field.set(object, value);
	}

}
