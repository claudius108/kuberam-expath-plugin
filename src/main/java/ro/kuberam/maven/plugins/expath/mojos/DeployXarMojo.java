package ro.kuberam.maven.plugins.expath.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugins.annotations.Mojo;

import ro.kuberam.maven.plugins.mojos.KuberamAbstractMojo;

/**
 * Deploys a package in a repository (TBD). <br/>
 * 
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius Teodorescu</a>
 * 
 */

@Mojo(name = "deploy-xar")
public class DeployXarMojo extends KuberamAbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub

	}

}
