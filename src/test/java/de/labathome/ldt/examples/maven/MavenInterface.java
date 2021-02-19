package de.labathome.ldt.examples.maven;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

/**
 * A minimalistic interface to Maven for loading artifacts and their
 * dependencies from Maven Central.
 * 
 * @author Jonathan Schilling (jonathan.schilling@mail.de)
 * @author Original Implementation by Jakob Svensson and Sehyun Kwak.
 */
public class MavenInterface {

	public static final File DEFAULT_MAVEN_HOME = new File(System.getProperty("user.home"), ".m2");
	public static final File DEFAULT_MAVEN_LOCAL_REPOSITORY = new File(DEFAULT_MAVEN_HOME, "repository");

	private List<RemoteRepository> mavenCentral;
	protected DefaultServiceLocator locator;

	public MavenInterface() {
		mavenCentral = new LinkedList<>();
		mavenCentral.add(
				new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());

		locator = MavenRepositorySystemUtils.newServiceLocator();
	}

	public String getDefaultLocalRepository() {
		return DEFAULT_MAVEN_LOCAL_REPOSITORY.getAbsolutePath();
	}

	public Artifact getArtifact(Artifact artifact) {
		RepositorySystem system = locator.getService(RepositorySystem.class);
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepository = new LocalRepository(getDefaultLocalRepository());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));

		ArtifactRequest request = new ArtifactRequest(artifact, mavenCentral, null);
		ArtifactResolver resolver = locator.getService(ArtifactResolver.class);
		try {
			return resolver.resolveArtifact(session, request).getArtifact();
		} catch (ArtifactResolutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Dependency> getDependencies(Artifact artifact) {
		RepositorySystem system = locator.getService(RepositorySystem.class);
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepository = new LocalRepository(getDefaultLocalRepository());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));

		ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest(artifact, mavenCentral, null);
		try {
			ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);
			return descriptorResult.getDependencies();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
