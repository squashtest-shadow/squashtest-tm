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
package org.squashtest.tm.web.internal.controller.search;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseSearchCriteria;
import org.squashtest.tm.domain.testcase.TestCaseStatus;

public class TestCaseSearchCriteriaAdapter implements TestCaseSearchCriteria {

	private String nameFilter = null;
	private boolean groupByProject = false;
	private String[] importances;
	private String[] natures;
	private String[] types;
	private String[] statuses;

	public TestCaseSearchCriteriaAdapter(String name, boolean groupByProject,
			String[] importances, String[] natures, String[] types, String[] statuses) {
		super();
		setName(name);
		isGroupByProject(groupByProject);
		setImportanceFilter(importances);
		setNatureFilter(natures);
		setTypeFilter(types);
		setStatusFilter(statuses);
	}

	@Override
	public String getNameFilter() {
		return nameFilter;
	}

	@Override
	public boolean usesNameFilter(){
		return StringUtils.isNotBlank(nameFilter);
	}

	@Override
	public boolean isGroupByProject() {
		return groupByProject;
	}

	@Override
	public boolean usesImportanceFilter(){
		return (importances.length > 0);
	}

	@Override
	public boolean usesNatureFilter(){
		return (natures.length > 0);
	}

	@Override
	public boolean usesTypeFilter(){
		return (types.length > 0);
	}

	@Override
	public boolean usesStatusFilter(){
		return (statuses.length > 0);
	}

	@Override
	public boolean includeFoldersInResult(){
		//naive, arbitrary implementation here
		//means that we do not filter on any testcase-specific attributes
		return ! (
				usesImportanceFilter()  ||
				usesNatureFilter()		||
				usesTypeFilter()		||
				usesStatusFilter()
				);
	}


	@Override
	public List<TestCaseImportance> getImportanceFilterSet() {

		List<TestCaseImportance> result = new LinkedList<TestCaseImportance>();

		for (String str : importances){
			result.add(TestCaseImportance.valueOf(str));
		}

		return result;

	}

	@Override
	public List<String> getNatureFilterSet() {
		return Arrays.asList(natures);
	}

	@Override
	public List<String> getTypeFilterSet() {

		return Arrays.asList(natures);

	}

	@Override
	public List<TestCaseStatus> getStatusFilterSet() {

		List<TestCaseStatus> result = new LinkedList<TestCaseStatus>();

		for (String str : statuses){
			result.add(TestCaseStatus.valueOf(str));
		}

		return result;

	}

	public final void setName(String name){
		this.nameFilter=name;
	}

	public final void isGroupByProject(boolean groupByProject){
		this.groupByProject=groupByProject;
	}

	public final void setImportanceFilter(String[] importances){ //NOSONAR no, this array is definitely not stored directly.
		if (importances == null){
			this.importances=new String[0];
		}else{
			this.importances = Arrays.copyOf(importances, importances.length);
		}

	}

	public final void setNatureFilter(String[] natures){ //NOSONAR no, this array is definitely not stored directly.
		if (natures == null){
			this.natures=new String[0];
		}else{
			this.natures = Arrays.copyOf(natures, natures.length);
		}

	}

	public final void setTypeFilter(String[] types){ //NOSONAR no, this array is definitely not stored directly.
		if (types == null){
			this.types=new String[0];
		}else{
			this.types = Arrays.copyOf(types, types.length);
		}

	}

	public final void setStatusFilter(String[] statuses){ //NOSONAR no, this array is definitely not stored directly.
		if (statuses == null){
			this.statuses=new String[0];
		}else{
			this.statuses = Arrays.copyOf(statuses, statuses.length);
		}

	}
}
