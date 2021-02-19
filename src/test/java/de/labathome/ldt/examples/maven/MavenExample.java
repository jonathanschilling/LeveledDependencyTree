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

public class MavenExample {

	public static void main(String[] args) {
		exampleApacheCommons();
	}

	public static class MavenDependency implements HasDependencies {

		private String coords;
		private Predicate<String> inTree;
		private List<HasDependencies> dependencies;

		public MavenDependency(Artifact artifact, Predicate<String> inTree, HasDependencies... dependencies) {
			this.coords = artifact.getGroupId()+":"+artifact.getArtifactId();

			this.inTree = inTree;

			this.dependencies = new LinkedList<>();
			for (HasDependencies dep : dependencies) {
				this.dependencies.add(dep);
			}
		}

		public void addDependency(HasDependencies dep) {
			this.dependencies.add(dep);
		}

		@Override
		public List<HasDependencies> getDependencies() {
			return this.dependencies;
		}

		@Override
		public boolean inTree() {
			if (this.inTree != null) {
				return this.inTree.test(this.coords);
			} else {
				throw new RuntimeException("inTree not set");
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof MavenDependency) {
				return this.coords.equals(((MavenDependency)o).coords);
			} else {
				return false;
			}
		}

		public String toString() {
			return coords;
		}
	}

	/**
	 * Determine the leveled dependency tree for Apache Commons.
	 */
	public static void exampleApacheCommons() {
		MavenInterface m = new MavenInterface();

		Artifact queryArtifact = new DefaultArtifact("com.github.fommil.netlib:all:pom:1.1.2");

		Artifact artifact = m.getArtifact(queryArtifact);
		System.out.println("artifact: " + artifact);

		Predicate<String> inTree = a -> {
			return a.startsWith("com.github.fommil");
		};

		// list of all artifacts over which a leveled dependency tree is to be generated
		List<HasDependencies> allDependencies = new LinkedList<>();

		// traverse tree and collect all in-tree dependencies
		List<Artifact> todoStack = new LinkedList<>();
		todoStack.add(artifact);

		while (todoStack.size() > 0) {
			Artifact currentArtifact = todoStack.get(0);
			MavenDependency current = new MavenDependency(currentArtifact, inTree);

			todoStack.remove(0);

			System.out.println("working on " + current);
			if (current.inTree()) {
				allDependencies.add(current);
				System.out.println(" in tree");

				List<Dependency> dependencies = m.getDependencies(currentArtifact);
				for (Dependency dependency : dependencies) {
					MavenDependency dep = new MavenDependency(dependency.getArtifact(), inTree);
					
					// need to re-construct dependency structure also in the parallel MavenDependecy tree
					current.addDependency(dep);
					
					// brute-force check...
					boolean inTodo = false;
					for (Artifact a: todoStack) {
						MavenDependency aD = new MavenDependency(a, inTree);
						if (aD.equals(dep)) {
							inTodo = true;
							break;
						}
					}
					
					if (dep.inTree() && !allDependencies.contains(dep) && !inTodo) {
						todoStack.add(dependency.getArtifact());
						System.out.println(" need to check dep: " + dependency.getArtifact().getGroupId()+":"+dependency.getArtifact().getArtifactId());
					}
				}
			}
			
		}

		System.out.println("\n### collection of in-tree elements completed ###\n");

		Map<Integer, List<HasDependencies>> leveledDepTree = LeveledDependencyTree.getDependencyTree(allDependencies);
//		Map<Integer, List<HasDependencies>> leveledDepTree = LeveledDependencyTree.getDependencyTreeRecursive(allDependencies);

		System.out.println("\n### generation of dependency tree completed ###\n");

		// printout level structure
		for (Entry<Integer, List<HasDependencies>> entry: leveledDepTree.entrySet()) {
			System.out.println("level "+entry.getKey());
			for (HasDependencies dep: entry.getValue()) {
				System.out.println("  "+dep);
			}
		}
	}

}
