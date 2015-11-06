/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.model.json;

import static org.squashtest.tm.domain.EntityType.CAMPAIGN;
import static org.squashtest.tm.domain.EntityType.ITEM_TEST_PLAN;
import static org.squashtest.tm.domain.EntityType.ITERATION;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_VERSION;
import static org.squashtest.tm.domain.EntityType.TEST_CASE;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.chart.ChartType;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnRole;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.infolist.InfoListFinderService;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class JsonChartWizardData {

	private Map<EntityType, Set<ColumnPrototype>> columnPrototypes;

	private EnumSet<ChartType> chartTypes = EnumSet.allOf(ChartType.class);

	private Map<ColumnRole, EnumSet<Operation>> columRoles = new EnumMap<ColumnRole, EnumSet<Operation>>(
			ColumnRole.class);

	private Map<DataType, EnumSet<Operation>> dataTypes = new EnumMap<DataType, EnumSet<Operation>>(DataType.class);



	// don't use EnumMap here cuz the business want a custom order
	private Map<EntityType, String> entityTypes = new LinkedHashMap<EntityType, String>();

	private Map<String, EnumSet<? extends Level>> levelEnums = new HashMap<String, EnumSet<? extends Level>>();

	private EnumSet<ExecutionStatus> executionStatus = EnumSet.allOf(ExecutionStatus.class);

	private Map<String, Map<String, InfoList>> projectInfoList = new HashMap<String, Map<String, InfoList>>();

	public Map<String, Map<String, InfoList>> getProjectInfoList() {
		return projectInfoList;
	}

	@JsonSerialize(contentUsing = LevelEnumSerializer.class)
	public Map<String, EnumSet<? extends Level>> getLevelEnums() {
		return levelEnums;
	}

	@JsonSerialize(using = LevelEnumSerializer.class)
	public EnumSet<ExecutionStatus> getExecutionStatus() {
		return executionStatus;
	}


	public JsonChartWizardData(Map<EntityType, Set<ColumnPrototype>> columnPrototypes, List<Project> projects,
			InfoListFinderService infoListFinder) {

		this.columnPrototypes = columnPrototypes;
		populate(projects, infoListFinder);

	}

	private void populate(List<Project> projects, InfoListFinderService infoListFinder) {

		for (ColumnRole cr : ColumnRole.values()) {
			columRoles.put(cr, cr.getOperations());
		}

		for (DataType dt : DataType.values()) {
			dataTypes.put(dt, dt.getOperations());
		}

		addLevelEnum("TEST_CASE_STATUS", TestCaseStatus.class);
		addLevelEnum("test-case-execution-mode", TestCaseExecutionMode.class);
		addLevelEnum("TEST_CASE_IMPORTANCE", TestCaseImportance.class);
		addLevelEnum("REQUIREMENT_VERSION_CRITICALITY", RequirementCriticality.class);
		addLevelEnum("REQUIREMENT_VERSION_STATUS", RequirementStatus.class);


		entityTypes.put(REQUIREMENT, "icon-requirement");
		entityTypes.put(TEST_CASE, "icon-test-case");
		entityTypes.put(CAMPAIGN, "icon-campaign");
		entityTypes.put(ITERATION, "icon-iteration");
		entityTypes.put(ITEM_TEST_PLAN, "sq-icon-control_play");

		for (Project project : projects) {

			Map<String, InfoList> infoLists = new HashMap<String, InfoList>();

			infoLists.put("REQUIREMENT_VERSION_CATEGORY", project.getRequirementCategories());
			infoLists.put("TEST_CASE_NATURE", project.getTestCaseNatures());
			infoLists.put("TEST_CASE_TYPE", project.getTestCaseTypes());
			projectInfoList.put(project.getId().toString(), infoLists);
		}

		Map<String, InfoList> defaultList = new HashMap<String, InfoList>();

		defaultList.put("REQUIREMENT_VERSION_CATEGORY",
				infoListFinder.findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode()));
		defaultList.put("TEST_CASE_NATURE", infoListFinder.findByCode(SystemInfoListCode.TEST_CASE_NATURE.getCode()));
		defaultList.put("TEST_CASE_TYPE", infoListFinder.findByCode(SystemInfoListCode.TEST_CASE_TYPE.getCode()));
		projectInfoList.put("default", defaultList);
		
		// business want to regroup requirement and requirement version attribute under requirement
		Set<ColumnPrototype> reqVersionCol = columnPrototypes.get(REQUIREMENT_VERSION);
		columnPrototypes.get(REQUIREMENT).addAll(reqVersionCol);
		columnPrototypes.remove(REQUIREMENT_VERSION);

	}

	private <E extends Enum<E> & Level> void addLevelEnum(String name, Class<E> clazz) {
		levelEnums.put(name, EnumSet.allOf(clazz));
	}


	public Map<EntityType, Set<ColumnPrototype>> getColumnPrototypes() {
		return columnPrototypes;
	}

	public EnumSet<ChartType> getChartTypes() {
		return chartTypes;
	}

	public Map<EntityType, String> getEntityTypes() {
		return entityTypes;
	}

	public Map<ColumnRole, EnumSet<Operation>> getColumRoles() {
		return columRoles;
	}

	public Map<DataType, EnumSet<Operation>> getDataTypes() {
		return dataTypes;
	}

}
