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
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.domain.project.Project;

public interface CustomFieldValueDao {

	
	/**
	 * 'nuff said.
	 * @param newValue
	 */
	void persist(CustomFieldValue newValue);
	
	
	/**
	 * Will batch-create the custom field values for a new entity, according to the custom field bindings it is subject to
	 * 
	 * @param destEntityId the id of the bound entity
	 * @param entityType the BindableEntity of that entity
	 * @param boundProject project to which it belongs
	 */
	void createAllCustomFieldValues(@QueryParam("destEntityId") Long destEntityId, @QueryParam("entityType") BindableEntity entityType, @QueryParam("boundProject") Project boundProject);
	
	
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
	 * Delete all the custom field values related to a BoundEntity, identified by its id and BindableEntity
	 * 
	 * @param entityId
	 * @param entity
	 */
	void deleteAllForEntity(@QueryParam("entityId") Long entityId, @QueryParam("entityType") BindableEntity entity);

	
	/**
	 * Delete all the custom field values related to a bunch of bound entities
	 * 
	 * @param entityTpe
	 * @param entityIds
	 */
	void deleteAllForEntities(@QueryParam("entityType") BindableEntity entityTpe, @QueryParam("entityIds") List<Long> entityIds);
	

	
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
	
	
	/**
	 * Will copy all the custom field values of a {@link BoundEntity} to another BoundEntity. Those two BoundEntity are expected to be of same {@link BindableEntity}.
	 *  
	 * @param srcEntityId the id of the BoundEntity from which we will copy the custom field values
	 * @param entityType the BindableEntity that must be common for both and destination entities.
	 * @param destEntityId the id of the BoundEntity which will receive the copies of custom field values. 
	 */
	void copyCustomFieldValues(@QueryParam("srcEntityId") Long srcEntityId, @QueryParam("destEntityId") Long destEntityId, @QueryParam("entityType") BindableEntity entityType );


	/**
	 * Will copy the content of all the custom field values of a {@link BoundEntity} into the custom field values of another BoundEntity.
	 * The custom field values of the destination are expected to exist; they won't be created on the fly.  
	 * The two BoundEntity are expected to be of same {@link BindableEntity}.
	 *  
	 * @param srcEntityId the id of the BoundEntity from which we will copy the custom field values
	 * @param entityType the BindableEntity that must be common for both and destination entities.
	 * @param destEntityId the id of the BoundEntity which will receive the copies of custom field values. 
	 */	
	void copyCustomFieldValuesContent(@QueryParam("srcEntityId") Long srcEntityId, @QueryParam("destEntityId") Long destEntityId, @QueryParam("entityType") BindableEntity entityType );

}
