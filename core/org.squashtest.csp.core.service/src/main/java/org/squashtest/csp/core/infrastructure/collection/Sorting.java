package org.squashtest.csp.core.infrastructure.collection;

/**
 * Defines sorting parameters to apply when querying for a collection.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface Sorting {
	/**
	 * Sorting information : which column should be sorted
	 * 
	 * @return
	 */
	String getSortedAttribute();

	/**
	 * Sorting information : which order
	 * 
	 * @return
	 */
	SortOrder getSortingOrder();
}
