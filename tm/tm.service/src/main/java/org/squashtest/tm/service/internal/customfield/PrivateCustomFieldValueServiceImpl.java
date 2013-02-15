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
package org.squashtest.tm.service.internal.customfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.service.internal.repository.BoundEntityDao;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao.CustomFieldValuesPair;
import org.squashtest.tm.service.security.PermissionEvaluationService;

@Service("squashtest.tm.service.CustomFieldValueManagerService")
@Transactional
public class PrivateCustomFieldValueServiceImpl implements PrivateCustomFieldValueService {

	@Inject
	private CustomFieldValueDao customFieldValueDao;

	@Inject
	private CustomFieldBindingDao customFieldBindingDao;

	@Inject
	private BoundEntityDao boundEntityDao;

	@Inject
	private PermissionEvaluationService permissionService;


	public void setPermissionService(
			PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasCustomFields(BoundEntity boundEntity) {
		return boundEntityDao.hasCustomField(boundEntity.getBoundEntityId(), boundEntity.getBoundEntityType());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasCustomFields(Long boundEntityId, BindableEntity bindableEntity) {
		return boundEntityDao.hasCustomField(boundEntityId, bindableEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomFieldValue> findAllCustomFieldValues(BoundEntity boundEntity) {
		if (!permissionService.canRead(boundEntity)) {
			throw new AccessDeniedException("Access is denied");
		}
		return customFieldValueDao
				.findAllCustomValues(boundEntity.getBoundEntityId(), boundEntity.getBoundEntityType());
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomFieldValue> findAllCustomFieldValues(Long boundEntityId, BindableEntity bindableEntity) {

		BoundEntity boundEntity = boundEntityDao.findBoundEntity(boundEntityId, bindableEntity);
		
		if (!permissionService.canRead(boundEntity)) {
			throw new AccessDeniedException("Access is denied");
		}
		
		return findAllCustomFieldValues(boundEntity);
	}

	
	@Override
	// well I'll skip the security check for this one because we don't really want to kill the db
	public List<CustomFieldValue> findAllCustomFieldValues(Collection<? extends BoundEntity> boundEntities) {
		
		//first, because the entities might be of different kind we must segregate them.
		Map<BindableEntity, List<Long>> compositeIds = _breakEntitiesIntoCompositeIds(boundEntities);
		
		//second, one can now call the db and consolidate the result.
		List<CustomFieldValue> result = new ArrayList<CustomFieldValue>();
		
		for (Entry<BindableEntity, List<Long>> entry : compositeIds.entrySet()){
			
			result.addAll(customFieldValueDao.batchedFindAllCustomValuesFor(entry.getValue(), entry.getKey()));
			
		}
		
		return result;
		
	}
	
	// same : no sec, a gesture of mercy for the database
	@Override
	public List<CustomFieldValue> findAllCustomFieldValues(Collection<? extends BoundEntity> boundEntities, 
														   Collection<CustomField> restrictedToThoseCustomfields) {
		
		//first, because the entities might be of different kind we must segregate them.
		Map<BindableEntity, List<Long>> compositeIds = _breakEntitiesIntoCompositeIds(boundEntities);
		
		//second, one can now call the db and consolidate the result.
		List<CustomFieldValue> result = new ArrayList<CustomFieldValue>();
		
		for (Entry<BindableEntity, List<Long>> entry : compositeIds.entrySet()){
			
			result.addAll(customFieldValueDao.batchedRestrictedFindAllCustomValuesFor(entry.getValue(), entry.getKey(), restrictedToThoseCustomfields));
			
		}
		
		return result;
		
	}

	

	@Override
	public void cascadeCustomFieldValuesCreation(CustomFieldBinding binding) {

		List<BoundEntity> boundEntities = boundEntityDao.findAllForBinding(binding);

		for (BoundEntity entity : boundEntities) {
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

		List<CustomFieldValue> allValues = customFieldValueDao.findAllCustomValuesOfBindings(customFieldBindingIds);
		_deleteCustomFieldValues(allValues);
		
	}

	@Override
	public void createAllCustomFieldValues(BoundEntity entity) {
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForProjectAndEntity(entity.getProject()
				.getId(), entity.getBoundEntityType());

		for (CustomFieldBinding binding : bindings) {
			CustomFieldValue value = binding.createNewValue();
			value.setBoundEntity(entity);
			customFieldValueDao.persist(value);
		}

	}
	
	
	private void _deleteCustomFieldValues(List<CustomFieldValue> values){
		List<Long> valueIds = _collectValueIds(values);
		customFieldValueDao.deleteAll(valueIds);
	}
	
	@Override
	public void deleteAllCustomFieldValues(BoundEntity entity) {
		customFieldValueDao.deleteAllForEntity(entity.getBoundEntityId(), entity.getBoundEntityType());
	}

	@Override
	public void deleteAllCustomFieldValues(BindableEntity entityType, List<Long> entityIds) {
		customFieldValueDao.deleteAllForEntities(entityType, entityIds);
	}

	@Override
	public void copyCustomFieldValues(BoundEntity source, BoundEntity recipient) {

		List<CustomFieldValue> sourceValues = customFieldValueDao.findAllCustomValues(source.getBoundEntityId(),
				source.getBoundEntityType());

		for (CustomFieldValue value : sourceValues) {
			CustomFieldValue copy = value.copy();
			copy.setBoundEntity(recipient);
			customFieldValueDao.persist(copy);
		}

	}

	@Override
	public void copyCustomFieldValuesContent(BoundEntity source, BoundEntity recipient) {
		
		List<CustomFieldValuesPair> pairs = customFieldValueDao.findPairedCustomFieldValues(source.getBoundEntityType(), 
																							source.getBoundEntityId(), 
																							recipient.getBoundEntityId());
		
		for (CustomFieldValuesPair pair : pairs) {
			pair.copyContent();
		}
	}

	@Override
	public void update(Long customFieldValueId, String newValue) {

		CustomFieldValue changedValue = customFieldValueDao.findById(customFieldValueId);

		BoundEntity boundEntity = boundEntityDao.findBoundEntity(changedValue);

		if (!permissionService.hasMoreThanRead(boundEntity)) {
			throw new AccessDeniedException("access is denied");
		}

		changedValue.setValue(newValue);
	}
	
	
	@Override
	//basically it's a copypasta of createAllCustomFieldValues, with some extra code in it.
	public void migrateCustomFieldValues(BoundEntity entity){

		List<CustomFieldBinding> newBindings = customFieldBindingDao.findAllForProjectAndEntity(entity.getProject().getId(), 
																							   	entity.getBoundEntityType());

		List<CustomFieldValue> formerValues	= customFieldValueDao.findAllCustomValues(entity.getBoundEntityId(), 
																					  entity.getBoundEntityType());

		
		for (CustomFieldBinding binding : newBindings) {
			
			CustomFieldValue newValue = binding.createNewValue();
			
			for (CustomFieldValue formerValue : formerValues){
				if (formerValue.representsSameCustomField(newValue)){
					newValue.setValue(formerValue.getValue());
					break;
				}
			}
			
			newValue.setBoundEntity(entity);
			customFieldValueDao.persist(newValue);
		}	
		
		_deleteCustomFieldValues(formerValues);
		
	}

	
	@Override
	public void migrateCustomFieldValues(Collection<BoundEntity> entities) {
		for (BoundEntity entity : entities){
			migrateCustomFieldValues(entity);
		}
	}
	
	// *********************** private convenience methods ********************
	 
	private List<Long> _collectValueIds(List<CustomFieldValue> values){
		List<Long> ids = new ArrayList<Long>(values.size());
		CollectionUtils.collect(values, new IdCollector());
		return ids;
	}
	
	
	private Map<BindableEntity, List<Long>> _breakEntitiesIntoCompositeIds(Collection<? extends BoundEntity> boundEntities) {
		
		Map<BindableEntity, List<Long>> segregatedEntities = new HashMap<BindableEntity, List<Long>>(3); //3 is just a guess
		for (BoundEntity entity : boundEntities){
			List<Long> idList = segregatedEntities.get(entity.getBoundEntityType());
			if (idList == null){
				idList = new ArrayList<Long>();
				segregatedEntities.put(entity.getBoundEntityType(), idList);
			}
			idList.add(entity.getBoundEntityId());
		}
		return segregatedEntities;
	}
	


	private static final class IdCollector implements Transformer{
		@Override
		public Object transform(Object value) {
			return ((CustomFieldValue)value).getId();
		}
	}
	

}
