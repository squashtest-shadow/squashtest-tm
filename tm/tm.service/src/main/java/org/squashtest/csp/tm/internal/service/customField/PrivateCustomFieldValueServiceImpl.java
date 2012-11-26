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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.internal.repository.BoundEntityDao;
import org.squashtest.csp.tm.internal.repository.CustomFieldBindingDao;
import org.squashtest.csp.tm.internal.repository.CustomFieldValueDao;
import org.squashtest.csp.tm.internal.repository.CustomFieldValueDao.CustomFieldValuesPair;

@Service("squashtest.tm.service.CustomFieldValueManagerService")
public class PrivateCustomFieldValueServiceImpl implements
		PrivateCustomFieldValueService {

	@Inject
	CustomFieldValueDao customFieldValueDao;
	
	@Inject 
	CustomFieldBindingDao customFieldBindingDao;
	

	@Inject
	private BoundEntityDao boundEntityDao;
	

	private PermissionEvaluationService permissionService;
	
	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	
	
	@Override
	public boolean hasCustomFields(BoundEntity boundEntity) {
		return boundEntityDao.hasCustomField(boundEntity.getBoundEntityId(), boundEntity.getBoundEntityType());
	}
	
	
	@Override
	public boolean hasCustomFields(Long boundEntityId,
			BindableEntity bindableEntity) {
		return boundEntityDao.hasCustomField(boundEntityId, bindableEntity);
	}

	@Override
	public List<CustomFieldValue> findAllCustomFieldValues(BoundEntity boundEntity) {
		if (! permissionService.canRead(boundEntity)){
			throw new AccessDeniedException("Access is denied");
		}
		return customFieldValueDao.findAllCustomValues(boundEntity.getBoundEntityId(), boundEntity.getBoundEntityType());
	}
	
	
	@Override
	public List<CustomFieldValue> findAllCustomFieldValues(Long boundEntityId,
			BindableEntity bindableEntity) {
		
		BoundEntity boundEntity = boundEntityDao.findBoundEntity(boundEntityId, bindableEntity);
		return findAllCustomFieldValues(boundEntity);
	}

	
	@Override
	public void cascadeCustomFieldValuesCreation(CustomFieldBinding binding) {
		
		List<BoundEntity> boundEntities = boundEntityDao.findAllForBinding(binding);
		
		for (BoundEntity entity : boundEntities){
			CustomFieldValue value = binding.createNewValue();
			value.setBoundEntity(entity);			
			customFieldValueDao.persist(value);		
		}
	}
	
	@Override
	public void cascadeCustomFieldValuesDeletion(CustomFieldBinding binding) {
		customFieldValueDao.deleteAllForBinding(binding.getId());	
	}
	
	@Override
	public void cascadeCustomFieldValuesDeletion(List<Long> customFieldBindingIds) {

		List<CustomFieldValue> allValues =  customFieldValueDao.findAllCustomValuesOfBindings(customFieldBindingIds);

		List<Long> ids = new ArrayList<Long>(allValues.size());
		for (CustomFieldValue value : allValues){
			ids.add(value.getId());
		}
		
		customFieldValueDao.deleteAll(ids);
	}
	
	
	@Override
	public void createAllCustomFieldValues(BoundEntity entity) {
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForProjectAndEntity(entity.getProject().getId(), entity.getBoundEntityType());
		
		for (CustomFieldBinding binding : bindings){			
			CustomFieldValue value = binding.createNewValue();
			value.setBoundEntity(entity);			
			customFieldValueDao.persist(value);		
		}
		
	}


	@Override
	public void deleteAllCustomFieldValues(BoundEntity entity) {
		customFieldValueDao.deleteAllForEntity(entity.getBoundEntityId(), entity.getBoundEntityType());
	}
		
	@Override
	public void deleteAllCustomFieldValues(BindableEntity entityType,
			List<Long> entityIds) {
		customFieldValueDao.deleteAllForEntities(entityType, entityIds);		
	}
	
	
	@Override
	public void copyCustomFieldValues(BoundEntity source, BoundEntity recipient) {
		
		List<CustomFieldValue> sourceValues = customFieldValueDao.findAllCustomValues(source.getBoundEntityId(), source.getBoundEntityType());
		
		for (CustomFieldValue value : sourceValues){
			CustomFieldValue copy = value.copy();
			copy.setBoundEntity(recipient);
			customFieldValueDao.persist(copy);
		}
	
	}

	@Override
	public void copyCustomFieldValuesContent(BoundEntity source, BoundEntity recipient) {
		List<CustomFieldValuesPair> pairs = customFieldValueDao.findPairedCustomFieldValues(source.getBoundEntityType(), source.getBoundEntityId(), recipient.getBoundEntityId());
		for (CustomFieldValuesPair pair : pairs){
			pair.copyContent();
		}
	}
	
	@Override
	public void update(Long customFieldValueId, String newValue) {

		CustomFieldValue changedValue = customFieldValueDao.findById(customFieldValueId);
		
		BoundEntity boundEntity = boundEntityDao.findBoundEntity(changedValue);
		
		if (! permissionService.hasMoreThanRead(boundEntity)){
			throw new AccessDeniedException("access is denied");
		}
		
		changedValue.setValue(newValue);
	}

}
