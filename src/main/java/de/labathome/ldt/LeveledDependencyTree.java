package de.labathome.ldt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements two methods (one flat and one recursive) to build a
 * leveled dependency tree from a list of objects depending on another.
 * 
 * @author Jonathan Schilling (jonathan.schilling@mail.de)
 */
public class LeveledDependencyTree {

	/** enable verbal diarrhea for debugging */
	public static boolean _debug = false;

	/**
	 * A flat algorithm to build a leveled dependency tree.
	 * 
	 * @param dependencies a flat list of objects depending on another
	 * @return Map<level, List<object in level>> leveled dependency tree
	 */
	public static Map<Integer, List<HasDependencies>> getDependencyTree(List<HasDependencies> artifacts) {

		Map<Integer, List<HasDependencies>> levels = new HashMap<>();

		int numRemaining = artifacts.size();
		while (numRemaining > 0) {

			List<HasDependencies> targetCurrentLevel = new LinkedList<>();

			// find all artifacts that only depend on previous levels or out-of-tree
			// artifacts
			for (HasDependencies fullId : artifacts) {

				// check that artifact itself is not in any previous levels
				boolean artifactInAnyPreviousLevels = false;
				for (List<HasDependencies> level : levels.values()) {
					artifactInAnyPreviousLevels |= level.contains(fullId);
				}

				if (!artifactInAnyPreviousLevels) {
					if (_debug) {
						System.out.println("check " + fullId);
					}

					// check if all in-tree dependencies are in previous levels
					boolean allInTreeDepsInPreviousLevels = true;
					for (HasDependencies dep : fullId.getDependencies()) {
						if (dep.inTree()) {
							boolean inPreviousLevels = false;
							for (List<HasDependencies> level : levels.values()) {
								inPreviousLevels |= level.contains(dep);
							}

							allInTreeDepsInPreviousLevels &= inPreviousLevels;
						}
					}

					if (_debug && allInTreeDepsInPreviousLevels) {
						System.out.println("  all deps are in previous level(s)");
					}

					// if all criteria are met, append the current artifact to the current leveld
					if (allInTreeDepsInPreviousLevels) {

						// append current artifact to current level
						targetCurrentLevel.add(fullId);
						numRemaining--;
					}
				}
			}

			// only put into list of levels after all artifacts have been checked!
			levels.put(levels.size() + 1, targetCurrentLevel);
		}

		return levels;
	}

	/**
	 * A recursive algorithm to build a leveled dependency tree.
	 * 
	 * @param dependencies a flat list of objects depending on another
	 * @return Map<level, List<object in level>> leveled dependency tree
	 * @author Original implementation by Udo Hoefel (udo@hoefel.eu)
	 */
	public static Map<Integer, List<HasDependencies>> getDependencyTreeRecursive(List<HasDependencies> dependencies) {
		Map<Integer, List<HasDependencies>> ret = new HashMap<>();

		for (HasDependencies dependency : dependencies) {

			if (_debug) {
				System.out.print("checking level of " + dependency + "... ");
			}
			int level = determineLevel(dependency, dependencies);
			if (_debug) {
				System.out.println(" => found " + level + ".");
			}

			ret.putIfAbsent(level, new LinkedList<>());

			ret.get(level).add(dependency);
		}

		return ret;
	}

	/**
	 * Determine the level of a given object within the given list of objects
	 * depending on another.
	 * 
	 * @param query        depending object
	 * @param dependencies list of objects depending on another
	 * @return the level of the object to query (starting at 1 for the root)
	 * @author Original implementation by Udo Hoefel (udo@hoefel.eu)
	 */
	private static int determineLevel(HasDependencies query, List<HasDependencies> dependencies) {
		return determineLevel(query, dependencies, 0);
	}

	/**
	 * Determine the level of a given object within the given list of objects
	 * depending on another.
	 * 
	 * @param query        depending object
	 * @param dependencies list of objects depending on another
	 * @param startlevel  
	 * @return the level of the object to query (starting at 1 for the root)
	 * @author Original implementation by Udo Hoefel (udo@hoefel.eu)
	 */
	private static int determineLevel(HasDependencies query, List<HasDependencies> dependencies, int startlevel) {
		int idxOfQuery = dependencies.indexOf(query);
		if (idxOfQuery != -1) {
			HasDependencies dep = dependencies.get(idxOfQuery);
			int level = startlevel + 1;
			for (HasDependencies deps : dep.getDependencies()) {
				level = Math.max(level, determineLevel(deps, dependencies, startlevel + 1));
			}
			return level;
		} else if (_debug) {
			System.out.println("not found in tree: "+query);
		}
		return startlevel;
	}

}
