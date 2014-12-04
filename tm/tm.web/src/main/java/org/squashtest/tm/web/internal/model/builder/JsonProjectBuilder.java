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
package org.squashtest.tm.web.internal.model.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldBindingModel;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.model.json.JsonInfoList;
import org.squashtest.tm.web.internal.model.json.JsonProject;

@Component
public class JsonProjectBuilder {

	@Inject
	private CustomFieldBindingFinderService cufBindingService;

	@Inject
	private CustomFieldJsonConverter customFieldConverter;

	public JsonProjectBuilder(){
		super();
	}

	public JsonProject toSimpleJson(Project p){
		return JsonProject.toJson(p);
	}

	public JsonProject toExtendedProject(Project p){

		// basic properties
		JsonProject res = JsonProject.toJson(p);

		//  the info lists
		JsonInfoList categories = JsonInfoList.toJson(p.getRequirementCategories());
		JsonInfoList natures = JsonInfoList.toJson(p.getTestCaseNatures());
		JsonInfoList types = JsonInfoList.toJson(p.getTestCaseTypes());

		res.setRequirementCategories(categories);
		res.setTestCaseNatures(natures);
		res.setTestCaseTypes(types);

		// the custom field bindings
		Map<String, List<CustomFieldBindingModel>> cufBindings = new HashMap<String, List<CustomFieldBindingModel>>();

		for (BindableEntity entity : BindableEntity.values()){
			List<CustomFieldBinding> bindings = cufBindingService.findCustomFieldsForProjectAndEntity(p.getId(), entity);
			List<CustomFieldBindingModel> jsBindings = new ArrayList<CustomFieldBindingModel>(bindings.size());
			for (CustomFieldBinding binding : bindings){
				jsBindings.add(customFieldConverter.toJson(binding));
			}
			cufBindings.put(entity.toString(), jsBindings);
		}

		res.setCustomFieldBindings(cufBindings);

		return res;
	}

}
