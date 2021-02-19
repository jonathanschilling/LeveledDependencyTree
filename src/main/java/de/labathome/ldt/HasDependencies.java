package de.labathome.ldt;

import java.util.List;

/**
 * An interface which can be used to specify dependencies among objects. It
 * supports dependencies to objects which shall not be considered in the leveled
 * dependency tree. These shall be identifyable using an appropriate inTree()
 * predicate.
 * 
 * @author Jonathan Schilling (jonathan.schilling@mail.de)
 *
 */
public interface HasDependencies {

	/**
	 * Get the list of object that this object depends on.
	 * 
	 * @return a list of objects that this object depends on
	 */
	public List<HasDependencies> getDependencies();

	/**
	 * Check if the given object should be considered in-tree or if it should be
	 * ignored.
	 * 
	 * @return true if the object should be considered in the dependency tree; false
	 *         if it should be ignored.
	 */
	public boolean inTree();
}
