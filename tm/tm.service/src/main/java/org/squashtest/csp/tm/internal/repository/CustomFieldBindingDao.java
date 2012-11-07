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
import org.squashtest.tm.core.foundation.collection.Paging;

public interface CustomFieldBindingDao extends CustomCustomFieldBindingDao{
	

	List<CustomFieldBinding> findAllForProject(long projectId);
	
	List<CustomFieldBinding> findAllForProjectAndEntity(long projectId, BindableEntity boundEntity);
	
	List<CustomFieldBinding> findAllForProjectAndEntity(long projectId, BindableEntity boundEntity, Paging paging);
	
	List<Long> findIdsForCustomField(long customFieldId);
	
	void persist(CustomFieldBinding binding);
	
	Long countAllForProjectAndEntity(long projectId, BindableEntity boundEntity);
	
	/**
	 * Given an id, returns the list of all the entities binding the same project to the same entity.
	 * 
	 * @param id
	 * @return
	 */
	public List<CustomFieldBinding> findAllAlike(long id);

}
