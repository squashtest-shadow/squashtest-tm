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
package org.squashtest.tm.web.internal.model.json;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tools.ant.util.DateUtils;
import org.squashtest.tm.domain.execution.ExecutionStatus;


public class JsonIterationTestPlanItem{
	private long id;
	private ExecutionStatus executionStatus;
	private String name;
	private String lastExecutedOn;	//format ISO 8601 aka ATOM 
	private String lastExecutedBy;
	private boolean isTestCaseDeleted;
	
	public JsonIterationTestPlanItem(long id, ExecutionStatus executionStatus,
			String name, String lastExecutedOn, String lastExecutedBy, boolean isTestCaseDeleted) {
		super();
		this.id = id;
		this.executionStatus = executionStatus;
		this.name = name;
		this.lastExecutedOn = lastExecutedOn;
		this.lastExecutedBy = lastExecutedBy;
		this.isTestCaseDeleted = isTestCaseDeleted;
	}
	
	public JsonIterationTestPlanItem(long id, ExecutionStatus executionStatus,
			String name, Date lastExecutedOn, String lastExecutedBy, boolean isTestCaseDeleted) {
		super();
		this.id = id;
		this.executionStatus = executionStatus;
		this.name = name;
		this.lastExecutedOn = toISO8601(lastExecutedOn);
		this.lastExecutedBy = lastExecutedBy;
		this.isTestCaseDeleted = isTestCaseDeleted;
	}
	
		
	private String toISO8601(Date date){
		return new SimpleDateFormat(DateUtils.ISO8601_DATETIME_PATTERN).format(date);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastExecutedOn() {
		return lastExecutedOn;
	}

	public void setLastExecutedOn(String lastExecutedOn) {
		this.lastExecutedOn = lastExecutedOn;
	}

	public String getLastExecutedBy() {
		return lastExecutedBy;
	}

	public void setLastExecutedBy(String lastExecutedBy) {
		this.lastExecutedBy = lastExecutedBy;
	}

	public boolean isTestCaseDeleted() {
		return isTestCaseDeleted;
	}

	public void setTestCaseDeleted(boolean isTestCaseDeleted) {
		this.isTestCaseDeleted = isTestCaseDeleted;
	}
	
	
	
}