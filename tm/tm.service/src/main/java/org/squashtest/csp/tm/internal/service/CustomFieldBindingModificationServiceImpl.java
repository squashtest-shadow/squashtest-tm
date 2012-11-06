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
package org.squashtest.csp.tm.internal.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding.PositionAwareBindingList;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.internal.repository.BoundEntityDao;
import org.squashtest.csp.tm.internal.repository.CustomFieldBindingDao;
import org.squashtest.csp.tm.internal.repository.CustomFieldDao;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.service.customField.PrivateCustomFieldValueService;
import org.squashtest.csp.tm.service.CustomFieldBindingModificationService;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;

@Transactional
@Service("squashtest.tm.service.CustomFieldBindingService")
public class CustomFieldBindingModificationServiceImpl implements CustomFieldBindingModificationService{

	
	@Inject
	private CustomFieldDao customFieldDao;

	@Inject
	private ProjectDao projectDao;
	
	@Inject
	private CustomFieldBindingDao customFieldBindingDao; 
	
	@Inject 
	private PrivateCustomFieldValueService customValueService;
	
	@Override
	public List<CustomField> findAvailableCustomFields() {
		return customFieldDao.findAll();
	}
	
	@Override
	public List<CustomField> findAvailableCustomFields(long projectId,	BindableEntity entity) {
		return customFieldDao.findAllBindableCustomFields(projectId, entity);
	}

	@Override
	public List<CustomFieldBinding> findCustomFieldsForProject(long projectId) {
		return customFieldBindingDao.findAllForProject(projectId);
	}

	@Override
	public List<CustomFieldBinding> findCustomFieldsForProjectAndEntity(long projectId, BindableEntity entity) {
		return customFieldBindingDao.findAllForProjectAndEntity(projectId, entity);
	}
	
	@Override
	public PagedCollectionHolder<List<CustomFieldBinding>> findCustomFieldsForProjectAndEntity(
			long projectId, BindableEntity entity, Paging paging) {
		
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForProjectAndEntity(projectId, entity, paging);
		Long count = customFieldBindingDao.countAllForProjectAndEntity(projectId, entity);
		
		return new PagingBackedPagedCollectionHolder<List<CustomFieldBinding>>(paging, count, bindings);
	}
	
	
	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')")		
	public void addNewCustomFieldBinding(long projectId, BindableEntity entity, long customFieldId, CustomFieldBinding newBinding) {
		_createBinding(projectId, entity, customFieldId, newBinding);
		customValueService.updateCustomFieldValues(newBinding);
	}
	
	
	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')")	
	public void removeCustomFieldBindings(List<Long> bindingIds){
		customFieldBindingDao.removeCustomFieldBindings(bindingIds);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')")	
	public void moveCustomFieldbindings(List<Long> bindingIds, int newIndex) {
		
		if (! bindingIds.isEmpty()){
			
			List<CustomFieldBinding> bindingList = customFieldBindingDao.findAllAlike(bindingIds.get(0));
			PositionAwareBindingList reorderList = new PositionAwareBindingList(bindingList);
			reorderList.reorderItems(bindingIds, newIndex);			
			
		}

	}


	private void _createBinding(long projectId, BindableEntity entity, long customFieldId, CustomFieldBinding newBinding) {
		
		Project project = projectDao.findById(projectId);
		CustomField field = customFieldDao.findById(customFieldId);
		Long newIndex = customFieldBindingDao.countAllForProjectAndEntity(projectId, entity) + 1;
		
		newBinding.setBoundProject(project);
		newBinding.setBoundEntity(entity);
		newBinding.setCustomField(field);
		newBinding.setPosition(newIndex.intValue());
		
		customFieldBindingDao.persist(newBinding);		
		
	}

	
}
