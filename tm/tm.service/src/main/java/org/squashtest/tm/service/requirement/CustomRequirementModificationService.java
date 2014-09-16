/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.requirement;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.requirement.RequirementCriticality;

/**
 * Requirement modification services which cannot be dynamically generated.
 *
 * @author Gregory Fouquet
 * 
 */
public interface CustomRequirementModificationService {
	
	void rename(long reqId, @NotNull String newName);

	/**
	 * Increase the current version of the given requirement.
	 * 
	 * @param requirementId
	 */
	void createNewVersion(long requirementId);
	/**
	 * will change the requirement criticality and update the importance of any associated TestCase with importanceAuto == true.<br>
	 * (even through call steps) 
	 *
	 * @param requirementId
	 * @param criticality
	 */
	void changeCriticality(long requirementId, @NotNull RequirementCriticality criticality);
	
	
}