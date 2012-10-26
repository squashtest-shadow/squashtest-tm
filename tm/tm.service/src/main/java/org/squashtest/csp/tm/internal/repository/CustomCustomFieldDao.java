/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
/**
 * 
 * Facade for Custom-Field access methods which cannot be dynamically generated. 
 *
 */
public interface CustomCustomFieldDao {

	/**
	 * Will find all custom fields and return them ordered by their name.
	 * 
	 * @return the list of all existing {@link CustomField} ordered by {@link CustomField#getName()}
	 */
	List<CustomField> finAllOrderedByName();
	
	/**
	 * Will find all existing custom fields ordered according to the given {@link CollectionSorting} params.
	 * 
	 * @param filter the {@link CollectionSorting} param that holds order and paging requirements.
	 * @return the list of all existing {@link CustomField} ordered according to the {@link CollectionSorting} param
	 */
	List<CustomField> findSortedCustomFields(CollectionSorting filter);
	

	/**
	 * Will count all existing custom fields
	 * 
	 * @return the number of custom fields
	 */
	long countCustomFields();
}
