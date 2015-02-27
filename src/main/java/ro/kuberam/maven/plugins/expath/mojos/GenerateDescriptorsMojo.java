package ro.kuberam.maven.plugins.expath.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import ro.kuberam.maven.plugins.mojos.KuberamAbstractMojo;

/**
 * Generates the descriptors for a package (TBD). <br/>
 * 
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius Teodorescu</a>
 * 
 */

@Mojo(name = "generate-descriptors")
//@Execute(goal = "generate-descriptors")
public class GenerateDescriptorsMojo extends KuberamAbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		
	}

}
