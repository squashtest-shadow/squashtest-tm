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
package org.squashtest.tm.web.internal.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.testcase.ActionStepCollector;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldModel;


@Component
public class CustomFieldHelperService {

	private CustomFieldBindingFinderService cufBindingService;

	private CustomFieldValueManagerService cufValuesService;
	

	@Inject
	private CustomFieldJsonConverter converter;
	
	@ServiceReference
	public void setCustomFieldBindingFinderService(CustomFieldBindingFinderService service){
		this.cufBindingService=service;
	}
	
	@ServiceReference
	public void setManagerService(CustomFieldValueManagerService cufValuesService) {
		this.cufValuesService = cufValuesService;
	}
	


	public boolean hasCustomFields(BoundEntity entity){
		return cufValuesService.hasCustomFields(entity);
	}
	
	public List<CustomFieldModel> convertToJson(List<CustomField> customFields){
		List<CustomFieldModel> models = new ArrayList<CustomFieldModel>(customFields.size());
		for (CustomField field : customFields){
			models.add(converter.toJson(field));
		}
		return models;
	}


	/**
	 * Return the CustomFields referenced by the CustomFieldBindings for the given project and BindableEntity type, ordered by their position. 
	 * 
	 * @param projectId
	 * @param entityType
	 * @return
	 */
	public List<CustomField> findCustomFieldsForEntitiesAtLocation(long projectId, BindableEntity entityType, RenderingLocation location){
		
		List<CustomFieldBinding> bindings = cufBindingService.findCustomFieldsForProjectAndEntity(projectId, entityType);
		Collections.sort(bindings, new BindingSorter());
		
		
		List<CustomField> result = new ArrayList<CustomField>(bindings.size());
		
		for (CustomFieldBinding binding : bindings){
			if (binding.getRenderingLocations().contains(location) ){
				result.add(binding.getCustomField());
			}
		}
		
		return result;
		
	}
	

	
	
	public List<CustomFieldValue> findCustomFieldValuesForTestSteps(List<TestStep> testSteps){
		
		if (testSteps.isEmpty()){
			return Collections.emptyList();
		}
			
		List<ActionTestStep> actionSteps = new ActionStepCollector().collect(testSteps);
		
		return cufValuesService.findAllCustomFieldValues(actionSteps);
		
	}
	
	
	
	private static final class BindingSorter implements Comparator<CustomFieldBinding>{
		@Override
		public int compare(CustomFieldBinding o1, CustomFieldBinding o2) {
			return o1.getPosition() - o2.getPosition();
		}
	}
	
	

	
}
