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
package org.squashtest.csp.tm.internal.service.importer;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.tm.domain.requirement.RequirementCategory;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;

/**
 * 
 * @author mpagnon
 *
 */
/* package-private */class PseudoRequirementVersion implements Comparable<PseudoRequirementVersion>{
	private Integer rowNumber = 0;
	private Double version = null;
	private String label = "untitled";
	private String reference = null;
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;
	private RequirementCategory category = RequirementCategory.UNDEFINED;
	private RequirementStatus state = RequirementStatus.WORK_IN_PROGRESS;
	private String description = null;
	private Date createdOnDate = new Date();
	private String createdBy = "import";
	private PseudoRequirement pseudoRequirement;


	private static final Logger LOGGER = LoggerFactory.getLogger(PseudoRequirementVersion.class);
	

	public PseudoRequirementVersion(String label2, int rowNumber, PseudoRequirement pseudoRequirement) {
		setLabel(label2);
		setRowNumber(rowNumber);
		setPseudoRequirement(pseudoRequirement);
	}
	
	/* ***************************** formatters *********************************** */
	public String formatDescription(String description) {
		return description;
	}

	/* ***************************** getter and setters *********************************** */
	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
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
			this.criticality = RequirementCriticality.valueOf(cricicality.toUpperCase());
		}catch(IllegalArgumentException iae){
			LOGGER.warn(iae.getMessage());
		}catch(NullPointerException npe){
			LOGGER.warn(npe.getMessage());
		}
	}	
	
	public RequirementCategory getCategory() {
		return category;
	}

	public void setCategory(String category) {
		try{
			this.category = RequirementCategory.valueOf(category.toUpperCase());
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
			this.state = RequirementStatus.valueOf(state.toUpperCase());
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
	
	public PseudoRequirement getPseudoRequirement() {
		return pseudoRequirement;
	}

	public void setPseudoRequirement(PseudoRequirement pseudoRequirement) {
		this.pseudoRequirement = pseudoRequirement;
	}

	/* ***************************** end getter and setters *********************************** */
	private boolean notEmpty(String string){
		return (string != null && (!string.isEmpty()));
	}

	@Override
	public int compareTo(PseudoRequirementVersion o2) {

		Double o1Version = this.getVersion();
		boolean o1Null = o1Version == null;
		Double o2Version = o2.getVersion();
		boolean o2Null = o2Version == null;
		if( o1Null|| o2Null){
			if( o1Null && o2Null){
				return compareRowNumbers(this, o2);
			}else{
				if(o1Null){return -1;}
				else{return +1;}
			}
		}else{
			if(o1Version == o2Version){
				return compareRowNumbers(this, o2);
			}else{
				return o1Version.compareTo(o2Version);
			}
		}
	}
	
	private int compareRowNumbers(PseudoRequirementVersion o1, PseudoRequirementVersion o2){
		return o1.getRowNumber().compareTo(o2.getRowNumber());
	}

}
