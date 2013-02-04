package org.squashtest.tm.core.foundation.collection;


/**
 * Interface for data-filtering instructions. The string that the user is looking for is returned by {@link #getFilter()}, and the name of the filtered attribute (if any)
 * is returned by {@link #getFilteredAttribute()} (see the method comments for details). 
 * Because filtering requires significantly more processing, services and dao using it should first check {@link #usesFiltering()} first before triggering the additional 
 * filtering mecanisms.
 * 
 * 
 * @author bsiri
 *
 */
public interface Filtering {

	
	/**
	 * @return true if any filtering is required. 
	 */
	boolean usesFiltering();
	
	/**
	 * @return the String that the user is searching for
	 */
	String getFilter();
	
	
	/**
	 * @return null if the filter is to be applied to any relevant attribute, a non-null values is the name (qualified or not) of the sorted attribute if the filtering should be applied to only one specific attribute.
	 */
	String getFilteredAttribute(); 
	
}
