/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.service.internal.repository.BoundEntityDao;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao.CustomFieldValuesPair;
import org.squashtest.tm.service.library.AdvancedSearchService;
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
	
	@Inject
	private AdvancedSearchService advancedSearchService;

	public void setPermissionService(PermissionEvaluationService permissionService) {
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

		// first, because the entities might be of different kind we must segregate them.
		Map<BindableEntity, List<Long>> compositeIds = breakEntitiesIntoCompositeIds(boundEntities);

		// second, one can now call the db and consolidate the result.
		List<CustomFieldValue> result = new ArrayList<CustomFieldValue>();

		for (Entry<BindableEntity, List<Long>> entry : compositeIds.entrySet()) {

			result.addAll(customFieldValueDao.batchedFindAllCustomValuesFor(entry.getValue(), entry.getKey()));

		}

		return result;

	}

	// same : no sec, a gesture of mercy for the database
	@Override
	public List<CustomFieldValue> findAllCustomFieldValues(Collection<? extends BoundEntity> boundEntities,
			Collection<CustomField> restrictedToThoseCustomfields) {

		// first, because the entities might be of different kind we must segregate them.
		Map<BindableEntity, List<Long>> compositeIds = breakEntitiesIntoCompositeIds(boundEntities);

		// second, one can now call the db and consolidate the result.
		List<CustomFieldValue> result = new ArrayList<CustomFieldValue>();

		for (Entry<BindableEntity, List<Long>> entry : compositeIds.entrySet()) {

			result.addAll(customFieldValueDao.batchedRestrictedFindAllCustomValuesFor(entry.getValue(), entry.getKey(),
					restrictedToThoseCustomfields));

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
			if(BindableEntity.TEST_CASE.equals(entity.getBoundEntityType())){
				advancedSearchService.reindexTestCase(entity.getBoundEntityId());
			}
		}
	}

	@Override
	public void cascadeCustomFieldValuesDeletion(CustomFieldBinding binding) {
		customFieldValueDao.deleteAllForBinding(binding.getId());
	}

	@Override
	public void cascadeCustomFieldValuesDeletion(List<Long> customFieldBindingIds) {

		List<CustomFieldValue> allValues = customFieldValueDao.findAllCustomValuesOfBindings(customFieldBindingIds);
		for(CustomFieldValue value : allValues){
			BoundEntity boundEntity = boundEntityDao.findBoundEntity(value);
			if(BindableEntity.TEST_CASE.equals(boundEntity.getBoundEntityType())){
				advancedSearchService.reindexTestCase(boundEntity.getBoundEntityId());
			}
		}
		deleteCustomFieldValues(allValues);

	}

	@Override
	public void createAllCustomFieldValues(BoundEntity entity) {
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForProjectAndEntity(entity.getProject()
				.getId(), entity.getBoundEntityType());

		for (CustomFieldBinding binding : bindings) {
			if (!foundValue(binding, entity)) {
				CustomFieldValue value = binding.createNewValue();
				value.setBoundEntity(entity);
				customFieldValueDao.persist(value);
				
				if(BindableEntity.TEST_CASE.equals(entity.getBoundEntityType())){
					advancedSearchService.reindexTestCase(entity.getBoundEntityId());
				}
			}
		}

	}

	private boolean foundValue(CustomFieldBinding binding, BoundEntity entity) {
		return !customFieldValueDao.findAllCustomFieldValueOfBindingAndEntity(binding.getId(),
				entity.getBoundEntityId(), entity.getBoundEntityType()).isEmpty();
	}

	private void deleteCustomFieldValues(List<CustomFieldValue> values) {
		List<Long> valueIds = IdentifiedUtil.extractIds(values);
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

		List<CustomFieldValuesPair> pairs = customFieldValueDao.findPairedCustomFieldValues(
				source.getBoundEntityType(), source.getBoundEntityId(), recipient.getBoundEntityId());

		for (CustomFieldValuesPair pair : pairs) {
			pair.copyContent();
		}
	}

	@Override
	public void changeValue(long customFieldValueId, String newValue) {

		CustomFieldValue changedValue = customFieldValueDao.findById(customFieldValueId);

		BoundEntity boundEntity = boundEntityDao.findBoundEntity(changedValue);
	
		if (!permissionService.hasMoreThanRead(boundEntity)) {
			throw new AccessDeniedException("access is denied");
		}

		changedValue.setValue(newValue);
		
		if(BindableEntity.TEST_CASE.equals(boundEntity.getBoundEntityType())){
			advancedSearchService.reindexTestCase(boundEntity.getBoundEntityId());
		}
	}

	@Override
	// basically it's a copypasta of createAllCustomFieldValues, with some extra code in it.
	public void migrateCustomFieldValues(BoundEntity entity) {

		List<CustomFieldValue> valuesToUpdate = customFieldValueDao.findAllCustomValues(entity.getBoundEntityId(),
				entity.getBoundEntityType());
		if (entity.getProject() != null) {
			List<CustomFieldBinding> projectBindings = customFieldBindingDao.findAllForProjectAndEntity(entity
					.getProject().getId(), entity.getBoundEntityType());

			for (CustomFieldBinding binding : projectBindings) {

				CustomFieldValue updatedValue = binding.createNewValue();

				for (CustomFieldValue formerValue : valuesToUpdate) {
					if (formerValue.representsSameCustomField(updatedValue)) {
						updatedValue.setValue(formerValue.getValue());
						break;
					}
				}

				updatedValue.setBoundEntity(entity);
				customFieldValueDao.persist(updatedValue);
			}
		}
		deleteCustomFieldValues(valuesToUpdate);

	}

	@Override
	public void migrateCustomFieldValues(Collection<BoundEntity> entities) {
		for (BoundEntity entity : entities) {
			migrateCustomFieldValues(entity);
		}
	}

	// *********************** private convenience methods ********************

	private Map<BindableEntity, List<Long>> breakEntitiesIntoCompositeIds(
			Collection<? extends BoundEntity> boundEntities) {

		Map<BindableEntity, List<Long>> segregatedEntities = new HashMap<BindableEntity, List<Long>>();

		for (BoundEntity entity : boundEntities) {
			List<Long> idList = segregatedEntities.get(entity.getBoundEntityType());

			if (idList == null) {
				idList = new ArrayList<Long>();
				segregatedEntities.put(entity.getBoundEntityType(), idList);
			}
			idList.add(entity.getBoundEntityId());
		}
		return segregatedEntities;
	}

}
