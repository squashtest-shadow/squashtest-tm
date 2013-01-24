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
package org.squashtest.csp.tm.internal.service.denormalizedField;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.csp.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.internal.repository.CustomFieldBindingDao;
import org.squashtest.csp.tm.internal.repository.CustomFieldValueDao;
import org.squashtest.csp.tm.internal.repository.DenormalizedFieldValueDao;

/**
 * 
 * @author mpagnon
 *
 */
@Service("squashtest.tm.service.DenormalizedFieldValueFinder")
public class PrivateDenormalizedFieldValueServiceImpl implements PrivateDenormalizedFieldValueService {
	@Inject
	private CustomFieldValueDao customFieldValueDao;
	@Inject
	private CustomFieldBindingDao customFieldBindingDao;
	@Inject
	private DenormalizedFieldValueDao denormalizedFieldValueDao;
	
	@Override
	public void createAllDenormalizedFieldValues(BoundEntity source, DenormalizedFieldHolder destination) {
		List<CustomFieldValue> customFieldValues = customFieldValueDao.findAllCustomValues(source.getBoundEntityId(), source.getBoundEntityType());
		for (CustomFieldValue customFieldValue : customFieldValues) {
			DenormalizedFieldValue dfv = new DenormalizedFieldValue(customFieldValue, destination.getDenormalizedFieldHolderId(), destination.getDenormalizedFieldHolderType());
			denormalizedFieldValueDao.persist(dfv);
			
		}
	}

	@Override
	public void deleteAllDenormalizedFieldValues(DenormalizedFieldHolder entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createAllDenormalizedFieldValues(TestStep sourceStep, ExecutionStep destinationStep, Project project) {
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
			DenormalizedFieldValue dfv = new DenormalizedFieldValue(value, binding, destinationStep.getDenormalizedFieldHolderId(), destinationStep.getDenormalizedFieldHolderType());
			denormalizedFieldValueDao.persist(dfv);
		}
		//add remaining fields
		int newBindingPosition = lastBindingPosition +1;
		for(CustomFieldValue remainingCufValue : sourceStepCustomFieldValues){
			DenormalizedFieldValue dfv = new DenormalizedFieldValue(remainingCufValue, newBindingPosition,  destinationStep.getDenormalizedFieldHolderId(), destinationStep.getDenormalizedFieldHolderType());
			denormalizedFieldValueDao.persist(dfv);
			newBindingPosition++;
		}
		
	}

	private CustomFieldValue removeCustomFieldValueCorrespondingToBinding(CustomFieldBinding binding,
			List<CustomFieldValue> customFieldValues) {
		Iterator<CustomFieldValue> iterator = customFieldValues.iterator();
		
		while(iterator.hasNext()){
			CustomFieldValue cufValue = iterator.next();
			if(cufValue.getCustomField().getId() == binding.getCustomField().getId()){
				iterator.remove();
				return cufValue;
			}
		}
		return null;
	}

}
