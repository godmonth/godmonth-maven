package com.godmonth.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

/**
 * @author shenyue
 */
public class MavenClasspathGenerator {
	private RepositorySystem system;
	private RemoteRepository central;
	private LocalRepository localRepo;
	private String remoteRepoUrl;
	private String localRepoPath;

	public void setRemoteRepoUrl(String remoteRepoUrl) {
		this.remoteRepoUrl = remoteRepoUrl;
	}

	public void setLocalRepoPath(String localRepoPath) {
		this.localRepoPath = localRepoPath;
	}

	public void init() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		system = locator.getService(RepositorySystem.class);
		central = new RemoteRepository.Builder("central", "default", remoteRepoUrl).build();
		localRepo = new LocalRepository(localRepoPath);

	}

	public List<File> getJarFileList(String coords) throws DependencyCollectionException, DependencyResolutionException {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
		Dependency dependency = new Dependency(new DefaultArtifact(coords), "runtime");
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(dependency);
		collectRequest.addRepository(central);

		DependencyNode node = system.collectDependencies(session, collectRequest).getRoot();
		DependencyRequest dependencyRequest = new DependencyRequest();
		dependencyRequest.setRoot(node);
		DependencyResult resolveDependencies = system.resolveDependencies(session, dependencyRequest);
		List<ArtifactResult> artifactResults = resolveDependencies.getArtifactResults();
		List<File> fs = new ArrayList<File>();
		for (ArtifactResult artifactResult : artifactResults) {
			fs.add(artifactResult.getArtifact().getFile());
		}
		return fs;
		// PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
		// node.accept(nlg);
		// return nlg.getClassPath();
	}

	public static String createClasspath(List<File> files) {
		List<String> l = new ArrayList<String>();
		for (File file : files) {
			l.add(file.getAbsolutePath());
		}
		return StringUtils.join(l, ",");
	}

	public Pair<List<URL>, List<String>> getJarList(List<File> jarFileList) throws MalformedURLException {
		List<URL> urls = new ArrayList<URL>();
		List<String> paths = new ArrayList<String>();

		for (File file : jarFileList) {
			urls.add(file.toURI().toURL());
			paths.add(FilenameUtils.separatorsToUnix("/" + FilenameUtils.getPath(file.getAbsolutePath())
					+ file.getName()));
		}
		return Pair.of(urls, paths);
	}

}
