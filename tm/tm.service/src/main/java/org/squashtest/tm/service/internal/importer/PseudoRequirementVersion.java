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
package org.squashtest.tm.service.internal.importer;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersionImportMemento;

/**
 * Holds:
 * <ul>
 * <li>usual requirement version attributes such as
 * <ul>
 * <li>label</li>
 * <li>reference</li>
 * <li>status</li>
 * <li>...</li>
 * </ul>
 * </li>
 * <li>structural informations from excel file such as
 * <ul>
 * <li>it's {@link PseudoRequirement} owner</li>
 * <li>the rowNumber</li>
 * <li>the version (nb)</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * This class implements Comparable to sort PseudoRequirementVersion depending on their rowNumber.
 * 
 * @author mpagnon
 * 
 */
/* package-private */class PseudoRequirementVersion implements Comparable<PseudoRequirementVersion>,
RequirementVersionImportMemento {
	private Integer rowNumber = 0;
	private Double version = null;
	private String label = "untitled";
	private String reference = "";
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;
	private String category ;
	private RequirementStatus status = RequirementStatus.WORK_IN_PROGRESS;
	private String description = "";
	private Date createdOnDate = new Date();
	private String createdBy = RequirementImporter.DEFAULT_CREATED_BY;
	private PseudoRequirement pseudoRequirement;

	private static final Logger LOGGER = LoggerFactory.getLogger(PseudoRequirementVersion.class);

	public PseudoRequirementVersion(String label2, int rowNumber, PseudoRequirement pseudoRequirement) {
		this.label = label2;
		this.rowNumber = rowNumber;
		this.pseudoRequirement = pseudoRequirement;
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

	public String getName() {
		return label;
	}

	public void setName(String name) {
		if (notEmpty(label)) {
			this.label = name;
		}
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		if (notEmpty(reference)) {
			this.reference = reference;
		}
	}

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(String criticality) {
		if (criticality != null) {
			try {
				String criticalityUp = criticality.toUpperCase();
				this.criticality = RequirementCriticality.valueOf(criticalityUp);
			} catch (IllegalArgumentException iae) {
				LOGGER.warn(iae.getMessage());
			}
		}
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public RequirementStatus getStatus() {
		return status;
	}

	public void setStatus(String state) {
		if (state != null) {
			try {
				this.status = RequirementStatus.valueOf(state.toUpperCase());
			} catch (IllegalArgumentException iae) {
				LOGGER.warn(iae.getMessage());
			}
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (notEmpty(description)) {
			this.description = description;
		}
	}

	public Date getCreatedOn() {
		return createdOnDate;
	}

	public void setCreatedOnDate(Date createdOnDate) {
		if (createdOnDate != null) {
			this.createdOnDate = createdOnDate;
		}
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		if (notEmpty(createdBy)) {
			this.createdBy = createdBy;
		}
	}

	public PseudoRequirement getPseudoRequirement() {
		return pseudoRequirement;
	}

	public void setPseudoRequirement(PseudoRequirement pseudoRequirement) {
		this.pseudoRequirement = pseudoRequirement;
	}

	/* ***************************** end getter and setters *********************************** */
	private boolean notEmpty(String string) {
		return (string != null && (!string.isEmpty()));
	}
	//GENERATED:START
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rowNumber == null) ? 0 : rowNumber.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PseudoRequirementVersion other = (PseudoRequirementVersion) obj;
		if (rowNumber == null) {
			if (other.rowNumber != null) {
				return false;
			}
		} else if (!rowNumber.equals(other.rowNumber)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(PseudoRequirementVersion o2) {
		int toreturn;
		if ((this.getVersion() == null) || (o2.getVersion() == null)) {
			if ((this.getVersion() == null) && (o2.getVersion() == null)) {
				toreturn = compareRowNumbers(this, o2);
			} else {
				if ((this.getVersion() == null)) {
					toreturn = -1;
				} else {
					toreturn = +1;
				}
			}
		} else {
			if (this.getVersion().compareTo(o2.getVersion()) == 0) {
				toreturn = compareRowNumbers(this, o2);
			} else {
				toreturn = this.getVersion().compareTo(o2.getVersion());
			}
		}
		return toreturn;
	}
	//GENERATED:END
	private int compareRowNumbers(PseudoRequirementVersion o1, PseudoRequirementVersion o2) {
		return o1.getRowNumber().compareTo(o2.getRowNumber());
	}

}
