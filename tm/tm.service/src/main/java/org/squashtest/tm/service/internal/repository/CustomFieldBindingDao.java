/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
@DynamicDao(entity=CustomFieldBinding.class)
public interface CustomFieldBindingDao extends CustomCustomFieldBindingDao {
	

	CustomFieldBinding findById(long bindingId);
	
	List<CustomFieldBinding> findAllForGenericProject(long projectId);
	
	List<CustomFieldBinding> findAllForProjectAndEntity(long projectId, BindableEntity boundEntity);
	
	List<CustomFieldBinding> findAllForProjectAndEntity(long projectId, BindableEntity boundEntity, Paging paging);
	
	List<CustomFieldBinding> findAllForCustomField(long customFieldId);
	
	void persist(CustomFieldBinding binding);
	
	Long countAllForProjectAndEntity(long projectId, BindableEntity boundEntity);
	
	/**
	 * Given an id, returns the list of all the entities binding the same project to the same entity.
	 * 
	 * @param id
	 * @return
	 */
	List<CustomFieldBinding> findAllAlike(long id);
	
	/**
	 * Given a bound entity, find which custom field bindings are effectively honored. 
	 * Indeed there is no constraints on the database that forces an entity to perfectly 
	 * match the custom field bindings that were defined at the project level for that entity. 
	 * 
	 * @param boundEntityId
	 * @param boundEntityType
	 * @return
	 */
	List<CustomFieldBinding> findEffectiveBindingsForEntity(@QueryParam("entityId") long entityId, 
															@QueryParam("entityType") BindableEntity entityType);
	
	
	/**
	 * batched version of {@link #findEffectiveBindingsForEntity(long, BindableEntity)}.
	 * 
	 * The result set is a bit different as it returns a tuple-2 : [ entityId, CustomFieldBinding]. 
	 * The first element of the array is an entityId, and the second is a CustomFieldBinding. 
	 * If an entity (of a given id) has multiple binding actually honored, multiple tuples will be 
	 * returned for that entity.
	 * 
	 * 
	 * @param entityIds
	 * @param entityType
	 * @return
	 */
	List<Object[]> findEffectiveBindingsForEntities(@QueryParam("entityIds") List<Long> entityIds, 
															@QueryParam("entityType") BindableEntity entityType);
	
	

}
