/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.denormalizedField;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedSingleSelectField;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.squashtest.tm.service.internal.repository.DenormalizedFieldValueDao;
import org.squashtest.tm.service.internal.repository.DenormalizedFieldValueDeletionDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;

/**
 * 
 * @author mpagnon
 *
 */
@Service("squashtest.tm.service.DenormalizedFieldValueManager")
public class PrivateDenormalizedFieldValueServiceImpl implements PrivateDenormalizedFieldValueService {
	@Inject
	private CustomFieldValueDao customFieldValueDao;
	@Inject
	private CustomFieldBindingDao customFieldBindingDao;
	@Inject
	private DenormalizedFieldValueDao denormalizedFieldValueDao;
	@Inject
	private DenormalizedFieldValueDeletionDao denormalizedFieldValueDeletionDao;
	@Inject
	private PermissionEvaluationService permissionService;


	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	@Override
	public void createAllDenormalizedFieldValues(BoundEntity source, DenormalizedFieldHolder destination) {
		List<CustomFieldValue> customFieldValues = customFieldValueDao.findAllCustomValues(source.getBoundEntityId(), source.getBoundEntityType());
		for (CustomFieldValue customFieldValue : customFieldValues) {
			if(customFieldValue.getCustomField().getInputType().equals(InputType.DROPDOWN_LIST)){
				DenormalizedSingleSelectField dfv = new DenormalizedSingleSelectField(customFieldValue, destination.getDenormalizedFieldHolderId(), destination.getDenormalizedFieldHolderType());
				denormalizedFieldValueDao.persist(dfv);
			} else {
				DenormalizedFieldValue dfv = new DenormalizedFieldValue(customFieldValue, destination.getDenormalizedFieldHolderId(), destination.getDenormalizedFieldHolderType());
				denormalizedFieldValueDao.persist(dfv);
			}
		}
	}

	@Override
	public void deleteAllDenormalizedFieldValues(DenormalizedFieldHolder entity) {
		List<DenormalizedFieldValue> dfvs = denormalizedFieldValueDao.findDFVForEntity(entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType());
		for(DenormalizedFieldValue dfv : dfvs){
			denormalizedFieldValueDeletionDao.removeDenormalizedFieldValue(dfv);
		}
	}

	@Override
	public void createAllDenormalizedFieldValues(ActionTestStep sourceStep, ExecutionStep destinationStep, Project project) {
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForProjectAndEntity(project.getId(), BindableEntity.TEST_STEP);
		List<CustomFieldValue> sourceStepCustomFieldValues = customFieldValueDao.findAllCustomValues(sourceStep.getBoundEntityId(), sourceStep.getBoundEntityType());
		int lastBindingPosition = 0;
		//create all field corresponding to project
		for(CustomFieldBinding binding : bindings){
			String value = "";
			CustomFieldValue projectCufValue = removeCustomFieldValueCorrespondingToBinding(binding, sourceStepCustomFieldValues);
			if(projectCufValue != null){
				value = projectCufValue.getValue();
			}
			lastBindingPosition = binding.getPosition();

			if(binding.getCustomField().getInputType().equals(InputType.DROPDOWN_LIST)){
				DenormalizedSingleSelectField dfv = new DenormalizedSingleSelectField(value, binding, destinationStep.getDenormalizedFieldHolderId(), destinationStep.getDenormalizedFieldHolderType());
				denormalizedFieldValueDao.persist(dfv);
			} else {
				DenormalizedFieldValue dfv = new DenormalizedFieldValue(value, binding, destinationStep.getDenormalizedFieldHolderId(), destinationStep.getDenormalizedFieldHolderType());
				denormalizedFieldValueDao.persist(dfv);
			}
		}
		//add remaining fields
		int newBindingPosition = lastBindingPosition +1;
		for(CustomFieldValue remainingCufValue : sourceStepCustomFieldValues){

			if(remainingCufValue.getCustomField().getInputType().equals(InputType.DROPDOWN_LIST)){
				DenormalizedSingleSelectField dfv = new DenormalizedSingleSelectField(remainingCufValue, newBindingPosition,  destinationStep.getDenormalizedFieldHolderId(), destinationStep.getDenormalizedFieldHolderType());
				denormalizedFieldValueDao.persist(dfv);
			} else {
				DenormalizedFieldValue dfv = new DenormalizedFieldValue(remainingCufValue, newBindingPosition,  destinationStep.getDenormalizedFieldHolderId(), destinationStep.getDenormalizedFieldHolderType());
				denormalizedFieldValueDao.persist(dfv);
			}

			newBindingPosition++;
		}

	}

	private CustomFieldValue removeCustomFieldValueCorrespondingToBinding(CustomFieldBinding binding,
			List<CustomFieldValue> customFieldValues) {
		Iterator<CustomFieldValue> iterator = customFieldValues.iterator();

		while(iterator.hasNext()){
			CustomFieldValue cufValue = iterator.next();
			if(cufValue.getCustomField().getId().equals(binding.getCustomField().getId())){
				iterator.remove();
				return cufValue;
			}
		}
		return null;
	}

	@Override
	public List<DenormalizedFieldValue> findAllForEntity(DenormalizedFieldHolder denormalizedFieldHolder) {
		return denormalizedFieldValueDao.findDFVForEntity(denormalizedFieldHolder.getDenormalizedFieldHolderId(), denormalizedFieldHolder.getDenormalizedFieldHolderType());
	}

	@Override
	public List<DenormalizedFieldValue> findAllForEntityAndRenderingLocation(
			DenormalizedFieldHolder denormalizedFieldHolder, RenderingLocation renderingLocation) {
		return denormalizedFieldValueDao.findDFVForEntityAndRenderingLocation(denormalizedFieldHolder.getDenormalizedFieldHolderId(), denormalizedFieldHolder.getDenormalizedFieldHolderType(), renderingLocation);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<DenormalizedFieldValue> findAllForEntities(Collection<DenormalizedFieldHolder> entities,Collection<RenderingLocation> nullOrLocations) {
		if (entities.isEmpty()){
			return Collections.emptyList();
		}
		else {
			DenormalizedFieldHolderType type = entities.iterator().next().getDenormalizedFieldHolderType();

			Collection<Long> entityIds = CollectionUtils.collect(entities, new Transformer() {
				@Override
				public Object transform(Object input) {
					return ((DenormalizedFieldHolder) input).getDenormalizedFieldHolderId();
				}
			});

			if (nullOrLocations == null){
				return denormalizedFieldValueDao.findDFVForEntities(type, entityIds);
			}
			else{
				return denormalizedFieldValueDao.findDFVForEntitiesAndLocations(type, entityIds, nullOrLocations);
			}
		}
	}

	@Override
	public List<DenormalizedFieldValue> findAllForEntity(Long denormalizedFieldHolderId, DenormalizedFieldHolderType denormalizedFieldHolderType) {
		return denormalizedFieldValueDao.findDFVForEntity(denormalizedFieldHolderId, denormalizedFieldHolderType);
	}

	@Override
	public void changeValue(long denormalizedFieldValueId, String newValue) {

		DenormalizedFieldValue changedValue = denormalizedFieldValueDao.findById(denormalizedFieldValueId);

		changedValue.setValue(newValue);
	}

	/**
	 * @see org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager#hasDenormalizedFields(org.squashtest.tm.domain.customfield.BoundEntity)
	 */
	@Override
	public boolean hasDenormalizedFields(DenormalizedFieldHolder entity) {
		return denormalizedFieldValueDao.countDenormalizedFields(entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType()) > 0;
	}

}
