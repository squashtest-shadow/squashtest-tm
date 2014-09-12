/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.SingleSelectField;
/**
 * 
 * Facade for Custom-Field access methods which cannot be dynamically generated. 
 *
 */
public interface CustomCustomFieldDao {
	
	/**
	 * Will find all existing custom fields ordered according to the given {@link PagingAndSorting} params.
	 * 
	 * @param filter the {@link PagingAndSorting} param that holds order and paging requirements.
	 * @return the list of all existing {@link CustomField} ordered according to the {@link PagingAndSorting} param
	 */
	List<CustomField> findSortedCustomFields(PagingAndSorting filter);
	

	
	/**
	 * will find the {@link SingleSelectField} of the given id
	 * @param customFieldId the id of the wanted {@link SingleSelectField}
	 * @return the {@link SingleSelectField} or <code>null</code>
	 */
	SingleSelectField findSingleSelectFieldById(Long customFieldId);
	
}
