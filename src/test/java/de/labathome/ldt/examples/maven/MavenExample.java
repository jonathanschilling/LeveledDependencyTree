package de.labathome.ldt.examples.maven;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.eclipse.aether.graph.Dependency;
import de.labathome.ldt.HasDependencies;
import de.labathome.ldt.LeveledDependencyTree;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * An example application of the LeveledDependecyTree class for finding out the
 * leveled dependency tree of a given Maven Artifact.
 * 
 * @author Jonathan Schilling (jonathan.schilling@mail.de)
 */
public class MavenExample {

	public static void main(String[] args) {
		exampleNetlibJava();
	}

	/**
	 * A surrogate class to mimic Maven Artifacts which implement the
	 * HasDependencies interface. This was deemed necessary since the Artifact class
	 * is final.
	 */
	public static class MavenArtifact implements HasDependencies {

		private List<HasDependencies> repository;
		private int id;
		private String name;
		private int[] dependencies;

		public MavenArtifact(List<HasDependencies> repository, String name) {
			this.repository = repository;
			this.repository.add(this);
			this.id = this.repository.size() - 1;
			this.name = name;
		}

		public void setDeps(int[] dependencies) {
			this.dependencies = dependencies;
		}

		public boolean hasDeps() {
			return dependencies != null;
		}

		@Override
		public List<HasDependencies> getDependencies() {
			List<HasDependencies> ret = new LinkedList<>();
			if (hasDeps()) {
				for (int i : dependencies) {
					ret.add(repository.get(i));
				}
			}
			return ret;
		}

		public int getId() {
			return id;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Check which is the index of a given Artifact, comparing only by groupId and
	 * artifactId.
	 * 
	 * @param haystack list of Artifacts
	 * @param needle   Artifact whose index in haystack is to be determined
	 * @return -1 of needle not in haystack; index of it otherwise (matched by
	 *         groupId and artifactId only)
	 */
	public static int indexOfArtifact(List<Artifact> haystack, Artifact needle) {
		int idxInRepo = -1;
		for (int i = 0; i < haystack.size(); ++i) {
			Artifact a = haystack.get(i);
			if (needle.getGroupId().equals(a.getGroupId()) && needle.getArtifactId().equals(a.getArtifactId())) {
				idxInRepo = i;
				break;
			}
		}
		return idxInRepo;
	}

	/**
	 * Determine the leveled dependency tree for netlib-java.
	 */
	public static void exampleNetlibJava() {
		MavenInterface m = new MavenInterface();

		Artifact queryArtifact = new DefaultArtifact("com.github.fommil.netlib:all:pom:1.1.2");
		Artifact artifact = m.getArtifact(queryArtifact);
		System.out.println("artifact: " + artifact);

		// use this to check if a given Artifact is to be considered in the dependency
		// tree
		Predicate<Artifact> inTree = a -> {
			return a.getGroupId().startsWith("com.github.fommil");
		};

		// list of all artifacts over which a leveled dependency tree is to be generated
		List<Artifact> artifactRepository = new LinkedList<>();
		List<HasDependencies> repository = new LinkedList<>();

		// traverse tree and collect all in-tree dependencies
		List<Artifact> todoStack = new LinkedList<>();
		todoStack.add(artifact);

		while (todoStack.size() > 0) {
			Artifact currentArtifact = todoStack.get(0);
			todoStack.remove(0);

			System.out.println("working on " + currentArtifact);
			if (inTree.test(currentArtifact)) {
				System.out.println("  in tree");

				// ensure that current artifact is registered in repository
				int idxInRepo = indexOfArtifact(artifactRepository, currentArtifact);
				MavenArtifact current;
				if (idxInRepo != -1) {
					System.out.println("  found in repo: id=" + idxInRepo);
					current = (MavenArtifact) repository.get(idxInRepo);
				} else {
					artifactRepository.add(currentArtifact);
					current = new MavenArtifact(repository, currentArtifact.getArtifactId());
					System.out.println("  put into repo: id=" + current.getId());
				}

				List<Integer> depIds = new LinkedList<>();

				List<Dependency> dependencies = m.getDependencies(currentArtifact);
				for (Dependency dependency : dependencies) {
					Artifact depArtifact = dependency.getArtifact();

					boolean foundInRepo = false;
					int depInRepo = indexOfArtifact(artifactRepository, depArtifact);
					if (depInRepo != -1) {
						System.out.println("  dep found in repo: " + depArtifact + " with id=" + depInRepo);
						foundInRepo = true;
					} else {
						artifactRepository.add(depArtifact);
						MavenArtifact dep = new MavenArtifact(repository, depArtifact.getArtifactId()); // register in repository
						depInRepo = dep.getId();
						System.out.println("  dep not in repo: " + depArtifact + " => registered as id=" + depInRepo);
					}

					depIds.add(depInRepo);

					// Only determine dependencies of an Artifact if it is in-tree and its deps were
					// not determined yet.
					if (inTree.test(depArtifact) && !foundInRepo) {
						todoStack.add(dependency.getArtifact());
					}
				}

				current.setDeps(depIds.stream().mapToInt(i -> i).toArray());
			} else { // not in tree
				System.out.println("  not in tree, skip checking for dependencies");
			}

		}

		System.out.println("\n### collection of in-tree elements completed ###\n");

//		Map<Integer, List<HasDependencies>> leveledDepTree = LeveledDependencyTree.getDependencyTree(repository);
		Map<Integer, List<HasDependencies>> leveledDepTree = LeveledDependencyTree
				.getDependencyTreeRecursive(repository);

		System.out.println("\n### generation of dependency tree completed ###\n");

		// printout level structure
		for (Entry<Integer, List<HasDependencies>> entry : leveledDepTree.entrySet()) {
			int level = entry.getKey();
			if (level == 0) {
				System.out.println("out-of-tree dependencies:");
			} else {
				System.out.println("level " + level);
			}
			for (HasDependencies dep : entry.getValue()) {
				System.out.println("  " + artifactRepository.get(((MavenArtifact) dep).getId()));
			}
		}
		
		System.out.println("\n### dot graph ###\n");

		
		System.out.println(LeveledDependencyTree.ldt2dot(leveledDepTree));
	}

}
