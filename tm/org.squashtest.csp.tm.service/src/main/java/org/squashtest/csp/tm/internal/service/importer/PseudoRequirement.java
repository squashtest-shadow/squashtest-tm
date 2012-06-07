/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

package org.squashtest.csp.tm.internal.service.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.testcase.TestStep;

/**
 * 
 * @author mpagnon
 *
 */
/* package-private */class PseudoRequirement {
	private RequirementFolder folder = null;
	private Double id = null;
	private Double version = null;
	private String label = null;
	private String reference = null;
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;
	private RequirementStatus state = RequirementStatus.UNDER_REVIEW;
	private String description = null;
	private Date createdOnDate = new Date();
	private String createdBy = "import";


	private static final Logger LOGGER = LoggerFactory.getLogger(PseudoRequirement.class);
	

	public PseudoRequirement(String label2) {
		setLabel(label2);
	}
	
	/* ***************************** formatters *********************************** */
	public String formatDescription(String description) {
		return description;
	}

	/* ***************************** getter and setters *********************************** */
	public RequirementFolder getFolder() {
		return folder;
	}

	public void setFolder(RequirementFolder folder) {
		this.folder = folder;
	}

	public Double getId() {
		return id;
	}

	public void setId(Double id) {
		this.id = id;
	}

	public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(String cricicality) {
		try{
			this.criticality = RequirementCriticality.valueOf(cricicality);
		}catch(IllegalArgumentException iae){
			LOGGER.warn(iae.getMessage());
		}catch(NullPointerException npe){
			LOGGER.warn(npe.getMessage());
		}
	}

	
	public RequirementStatus getState() {
		return state;
	}

	public void setState(String state) {
		try{
			this.state = RequirementStatus.valueOf(state);
		}catch(IllegalArgumentException iae){
			LOGGER.warn(iae.getMessage());
		}catch(NullPointerException npe){
			LOGGER.warn(npe.getMessage());
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = formatDescription(description);
	}

	public Date getCreatedOnDate() {
		return createdOnDate;
	}

	public void setCreatedOnDate(Date createdOnDate) {
		if(createdOnDate != null){
		this.createdOnDate = createdOnDate;
		}
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		if(notEmpty(createdBy))
		this.createdBy = createdBy;
	}

	
	
	
	/* ***************************** end getter and setters *********************************** */
	private boolean notEmpty(String string){
		return (string != null && (!string.isEmpty()));
	}


}
