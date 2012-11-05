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

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.internal.repository.CustomFieldBindingDao;
import org.squashtest.csp.tm.internal.repository.CustomFieldValueDao;
import org.squashtest.csp.tm.service.customfield.CustomFieldValueManagerService;

@Service("CustomFieldValueManagerService")
public class CustomFieldValueManagerServiceImpl implements
		PrivateCustomFieldValueService {

	@Inject
	CustomFieldValueDao customFieldValueDao;
	
	@Inject 
	CustomFieldBindingDao customFieldBindingDao;
	
	private PermissionEvaluationService permissionService;
	
	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}


	@Override
	public List<CustomFieldValue> findAllCustomFieldValues(BoundEntity boundEntity) {
		if (! permissionService.canRead(boundEntity)){
			throw new AccessDeniedException("Access is denied");
		}
		return customFieldValueDao.findAllCustomValues(boundEntity.getBoundEntityId(), boundEntity.getBoundEntityType());
	}

	
	
	@Override
	public void createCustomFieldValues(BoundEntity entity) {
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForProjectAndEntity(entity.getProject().getId(), entity.getBoundEntityType());
		
		for (CustomFieldBinding binding : bindings){
			
			CustomFieldValue value = binding.createNewValue();
			value.setBoundEntity(entity);	
			
			customFieldValueDao.persist(value);
		}
	}


	@Override
	public void copyCustomFieldValues(BoundEntity source, BoundEntity dest) {
		List<CustomFieldValue> sourceValues = customFieldValueDao.findAllCustomValues(source.getBoundEntityId(), source.getBoundEntityType());
		
		for (CustomFieldValue value : sourceValues){
			CustomFieldValue copy = value.copy();
			copy.setBoundEntity(dest);
			customFieldValueDao.persist(copy);
		}
	}
	

}
