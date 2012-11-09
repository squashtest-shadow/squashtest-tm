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
package org.squashtest.csp.tm.internal.service.customField;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.service.customfield.CustomFieldValueManagerService;


/**
 * That interface is called so because it should remain private to this bundle. The reason is that the methods will not be secured. 
 * 
 * @author bsiri
 *
 */
@Transactional
public interface PrivateCustomFieldValueService extends CustomFieldValueManagerService {

	
	/**
	 * Will create a custom field value for all the entities affected by the given binding
	 * 
	 * @param binding
	 */
	void cascadeCustomFieldValuesCreation(CustomFieldBinding binding);
	
	
	/**
	 * Will remove the custom field values corresponding to the given binding
	 * 
	 * @param binding
	 */
	void cascadeCustomFieldValuesDeletion(CustomFieldBinding binding);
	
	
	/**
	 * Will remove the custom field values corresponding to the bindings, given their ids.
	 * 
	 * @param binding
	 */
	void cascadeCustomFieldValuesDeletion(List<Long> customFieldBindingIds);	
	
	
	/**
	 * Will create all the custom field values for one entity. 
	 * 
	 * @param entity
	 */
	void createAllCustomFieldValues(BoundEntity entity);
	
	
	/**
	 * will delete all the custom field vales for one entity
	 * 
	 * @param entity
	 */
	void deleteAllCustomFieldValues(BoundEntity entity);
	
	
	/**
	 * Will delete all the custom field values for multiple BoundEntities
	 * @param entityType the BindableEntity that all of the BoundEntity must share
	 * @param entityIds the ids of those BoundEntities
	 */
	void deleteAllCustomFieldValues(BindableEntity entityType, List<Long> entityIds);
	

	/**
	 * Will copy the custom field values from an entity to another entity, creating them in the process
	 * 
	 * @param entity
	 */
	void copyCustomFieldValues(BoundEntity source, BoundEntity recipient);
	
	
	/**
	 * Will copy the custom field values from an entity to another entity. It assumes that the custom field values
	 * already exists for both, and will simply invoke {@link CustomFieldValue#setValue(String)} from one to the other. 
	 * 
	 * 
	 * @param source
	 * @param dest
	 */
	void copyCustomFieldValuesContent(BoundEntity source, BoundEntity recipient);
}
