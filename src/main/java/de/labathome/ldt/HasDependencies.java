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
}
