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

import java.util.Date;

import org.squashtest.tm.core.foundation.lang.IsoDateUtils;

public class JsonExecutionInfo {

	private String modifiedOn;
	private String modifiedBy;
	private String executionStatus;
	private String automatedStatus;
	private String resultURL;
	
	public JsonExecutionInfo(){
		super();
	}

	public JsonExecutionInfo(String modifiedOn, String modifiedBy,
			String executionStatus, String automatedStatus, String resultURL) {
		super();
		this.modifiedOn = modifiedOn;
		this.modifiedBy = modifiedBy;
		this.executionStatus = executionStatus;
		this.automatedStatus = automatedStatus;
		this.resultURL = resultURL;
	}
	
	public JsonExecutionInfo(Date modifiedOn, String modifiedBy,
			String executionStatus, String automatedStatus, String resultURL) {
		super();
		this.modifiedOn = IsoDateUtils.formatIso8601DateTime(modifiedOn);
		this.modifiedBy = modifiedBy;
		this.executionStatus = executionStatus;
		this.automatedStatus = automatedStatus;
		this.resultURL = resultURL;
	}
	
}
