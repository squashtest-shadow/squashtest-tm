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
package org.squashtest.tm.service.internal.batchimport;

import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;

public class RequirementVersionTarget extends Target {

	private RequirementTarget requirement;

	private Integer version;
	
	private String name;




	public RequirementVersionTarget(RequirementTarget requirement, Integer version) {
		super();
		this.requirement = requirement;
		this.version = version;
	}


	public RequirementVersionTarget() {
		super();
	}
	
	public RequirementVersionTarget() {
		super();
	}


	@Override
	public EntityType getType() {
		return EntityType.REQUIREMENT_VERSION;
	}

	@Override
	public boolean isWellFormed() {
		return requirement!=null && requirement.isWellFormed();
	}

	@Override
	public String getProject() {
		return requirement.getProject();
	}

	@Override
	public String getPath() {
		return requirement.getPath();
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public RequirementTarget getRequirement(){
		return requirement;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((requirement == null) ? 0 : requirement.hashCode());
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
		RequirementVersionTarget other = (RequirementVersionTarget) obj;
		if (requirement == null) {
			if (other.requirement != null) {
				return false;
			}
		} else if (!requirement.equals(other.requirement)) {
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





}
