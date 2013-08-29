package com.godmonth.maven;

import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.testng.annotations.Test;

import com.godmonth.maven.MavenClasspathGenerator;

public class MavenClasspathGeneratorTest {

	@Test(enabled = true)
	public void test() throws DependencyCollectionException, DependencyResolutionException {
		MavenClasspathGenerator mcg = new MavenClasspathGenerator();
		mcg.setLocalRepoPath("target/local-repo");
		mcg.init();
		System.out.println(mcg.getJarFileList("org.apache.commons:commons-lang3:3.1"));
		System.out.println(mcg.getJarFileList("com.fasterxml.jackson.core:jackson-databind:2.2.1"));
	}
}
