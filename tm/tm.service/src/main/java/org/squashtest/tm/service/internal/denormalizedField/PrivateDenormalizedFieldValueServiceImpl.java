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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.IdCollector;
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
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.squashtest.tm.service.internal.repository.DenormalizedFieldValueDao;
import org.squashtest.tm.service.internal.repository.DenormalizedFieldValueDeletionDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
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

	@Inject
	private ExecutionDao execDao;


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
	public void createAllDenormalizedFieldValues(ActionTestStep sourceStep, ExecutionStep destinationStep, Project project){

		final List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForProjectAndEntity(project.getId(), BindableEntity.TEST_STEP);

		List<CustomFieldValue> stepCufs = customFieldValueDao.findAllCustomValues(sourceStep.getBoundEntityId(), sourceStep.getBoundEntityType());

		createDenormalizedFieldValuesForStep(destinationStep, stepCufs, bindings);

	}


	@Override
	public void createAllDenormalizedFieldValuesForSteps(Execution execution) {

		Project testcaseProject = execution.getReferencedTestCase().getProject();

		// fetch execution steps
		List<ExecutionStep> executionSteps = execDao.findExecutionSteps(execution.getId());
		if (executionSteps.isEmpty()){
			return;
		}

		// fetch the custom fields of the original steps
		List<Long> originalIds = execDao.findOriginalStepIds(execution.getId());
		List<CustomFieldValue> originalValues = customFieldValueDao.batchedInitializedFindAllCustomValuesFor(originalIds, BindableEntity.TEST_STEP);

		// sort them by original step id
		MultiValueMap cufsPerStepId = new MultiValueMap();
		for (CustomFieldValue value : originalValues){
			cufsPerStepId.put(value.getBoundEntityId(), value);
		}

		// fetch the custom field binding used as reference for the new ordering
		List<CustomFieldBinding> projectBindings = customFieldBindingDao.findAllForProjectAndEntity(testcaseProject.getId(), BindableEntity.TEST_STEP);

		// main loop
		for (ExecutionStep estep : executionSteps){
			@SuppressWarnings("unchecked")
			Collection<CustomFieldValue> cufValues = cufsPerStepId.getCollection(estep.getReferencedTestStep().getId());
			if (cufValues != null){ // might be null when there are no custom fields
				List<CustomFieldValue> values = new ArrayList<CustomFieldValue>(cufValues);
				createDenormalizedFieldValuesForStep(estep, values, projectBindings);
			}
		}

	}


	// will create the denormalized custom field values for a given exec steps. It takes care of
	// a specific reordering, putting first the cufs that references one of the bindings suppplied in the collection
	// and the others remain last.
	private void createDenormalizedFieldValuesForStep(ExecutionStep step, List<CustomFieldValue> values, Collection<CustomFieldBinding> bindings){

		List<CustomFieldValue> normalized = normalizedValues(step, values, bindings);

		// now loop and create the custom fields;
		for (CustomFieldValue cfv : normalized ){

			if(cfv.getCustomField().getInputType().equals(InputType.DROPDOWN_LIST)){
				DenormalizedSingleSelectField dfv = new DenormalizedSingleSelectField(cfv.getValue(), cfv.getBinding(),  step.getDenormalizedFieldHolderId(), step.getDenormalizedFieldHolderType());
				denormalizedFieldValueDao.persist(dfv);
			} else {
				DenormalizedFieldValue dfv = new DenormalizedFieldValue(cfv.getValue(), cfv.getBinding(),  step.getDenormalizedFieldHolderId(), step.getDenormalizedFieldHolderType());
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



	// ************************* private stuffs *****************************


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

	/*
	 * 1 - values that corresponds to a custom field referenced by the given bindings are inserted first
	 * with their value, according to the position of the binding, and respect their display location
	 * 2 - insert a dummy custom field value for each custom field that wasn't represented in the collection 'values'
	 *  according to the position and the display location
	 *  (XXX : is that a real functional requirement or just to make the datatable work ? Seems the later to me)
	 * 3 - insert the other custom fields with no specific display location
	 * 
	 */
	private List<CustomFieldValue> normalizedValues(ExecutionStep step, List<CustomFieldValue> values, Collection<CustomFieldBinding >bindings){

		List<CustomFieldValue> res = new LinkedList<CustomFieldValue>();

		List<CustomFieldValue> remainingValues = new ArrayList<CustomFieldValue>(values);

		// bullets 1 and 2
		int position = 0;
		for (CustomFieldBinding b : bindings){

			CustomFieldValue value = removeCustomFieldValueCorrespondingToBinding(b, remainingValues);

			if (value == null){
				value = new EmptyCustomFieldValue(step.getBoundEntityId(), step.getBoundEntityType(), b);
			}

			res.add(value);

			position++;
		}

		// bullets 3
		for (CustomFieldValue v : remainingValues){
			CustomFieldBinding dummyBinding = new CustomFieldBinding();
			dummyBinding.setCustomField(v.getBinding().getCustomField());
			dummyBinding.setPosition(position);

			res.add(new CustomFieldValue(step.getBoundEntityId(), step.getBoundEntityType(), dummyBinding, v.getValue()));

			position++;
		}

		return res;


	}

	// that class doesn't care of empty values
	private static final class EmptyCustomFieldValue extends CustomFieldValue{

		public String getValue(){
			return "";
		}

		public void setValue(String v){

		}

		public EmptyCustomFieldValue(Long boundEntityId, BindableEntity boundEntityType, CustomFieldBinding binding){

			this.boundEntityId = boundEntityId;
			this.boundEntityType = boundEntityType;
			this.binding = binding;

		}

	}


}
