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

import org.squashtest.csp.core.infrastructure.dynamicmanager.QueryParam;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;

public interface CustomFieldValueDao {

	
	/**
	 * 'nuff said.
	 * @param newValue
	 */
	void persist(CustomFieldValue newValue);
	
	/**
	 * 
	 * 'nuff said.
	 * @param value
	 */
	void delete(CustomFieldValue value);
	
	
	/**
	 * Delete all the CustomFieldValue, given their ids.
	 * 
	 * @param ids
	 */
	void deleteAll(@QueryParam("ids") List<Long> ids);
	
	
	
	/**
	 * Delete all the CustomFieldValue related to a {@link CustomFieldBinding}, given its id.
	 * 
	 * @param bindingId
	 */
	void deleteAllForBinding(@QueryParam("bindingId") Long bindingId);

	
	
	/**
	 * 'nuff said. 
	 * @param id
	 * @return
	 */
	CustomFieldValue findById(Long id);
	
	
	
	/**
	 * returns the list of {@link CustomFieldValue} for the given entity, sorted according to the 
	 * order specified in their respective {@link CustomFieldBinding}.
	 * 
	 * @param entityId
	 * @param entityType
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValues(long entityId, BindableEntity entityType);

	/**
	 * returns all the {@link CustomFieldValue} related to a given {@link CustomFieldBinding}, sorted according to
	 * their custom field binding order.
	 * 
	 * @param customFieldBindingId
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValuesOfBinding(long customFieldBindingId);

	
	/**
	 * returns all the CustomFieldValue related to a list of CustomFieldBinding, the resulting elements will be
	 * returned in unspecified order
	 * @param customFieldBindingIds
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValuesOfBindings(@QueryParam("bindingIds") List<Long>customFieldBindingIds);	
}
