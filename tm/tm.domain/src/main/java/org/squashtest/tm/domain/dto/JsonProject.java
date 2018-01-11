/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.domain.dto;

import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.project.Project;

import java.util.*;

/**
 * @author Gregory Fouquet
 *
 */
public class JsonProject {


	private long id;
	private String name;

	private Map<String, List<CustomFieldBindingModel>> customFieldBindings;

	private Set<JsonMilestone> milestones;

	private JsonInfoList requirementCategories;

	private JsonInfoList testCaseNatures;

	private JsonInfoList testCaseTypes;

	public JsonProject() {
		Map<String, List<CustomFieldBindingModel>> bindingsMap = new HashMap<>();
		EnumSet<BindableEntity> bindableEntities = EnumSet.allOf(BindableEntity.class);
		for (BindableEntity bindableEntity : bindableEntities) {
			bindingsMap.put(bindableEntity.name(), new ArrayList<CustomFieldBindingModel>());
		}
		this.customFieldBindings = bindingsMap;
	}

	public void addCustomFieldModel(CustomFieldBindingModel cfb){
		String bindableEntity = cfb.getBoundEntity().getEnumName();
		customFieldBindings.get(bindableEntity).add(cfb);
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return "/projects/" + id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, List<CustomFieldBindingModel>> getCustomFieldBindings() {
		return customFieldBindings;
	}

	public JsonInfoList getRequirementCategories() {
		return requirementCategories;
	}

	public JsonInfoList getTestCaseNatures() {
		return testCaseNatures;
	}

	public JsonInfoList getTestCaseTypes() {
		return testCaseTypes;
	}

	public void setCustomFieldBindings(Map<String, List<CustomFieldBindingModel>> customFieldBindings) {
		this.customFieldBindings = customFieldBindings;
	}

	public void setRequirementCategories(JsonInfoList requirementCategories) {
		this.requirementCategories = requirementCategories;
	}

	public void setTestCaseNatures(JsonInfoList testCaseNatures) {
		this.testCaseNatures = testCaseNatures;
	}

	public void setTestCaseTypes(JsonInfoList testCaseTypes) {
		this.testCaseTypes = testCaseTypes;
	}

	public Set<JsonMilestone> getMilestones() {
		return milestones;
	}

	public void setMilestones(Set<JsonMilestone> milestones) {
		this.milestones = milestones;
	}

}