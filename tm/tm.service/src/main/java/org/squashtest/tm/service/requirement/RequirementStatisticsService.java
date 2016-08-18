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
package org.squashtest.tm.service.requirement;

import java.util.Collection;

import org.squashtest.tm.service.statistics.requirement.RequirementBoundDescriptionStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementBoundTestCasesStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementCriticalityStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementStatisticsBundle;
import org.squashtest.tm.service.statistics.requirement.RequirementStatusesStatistics;

public interface RequirementStatisticsService {

	/**
	 * Given those requirements ids, returns how many of them are bound to test cases and how many aren't. Warning : no security check will 
	 * be performed and the data will be returned regardless of who requested it.
	 * 
	 * @param requirementIds
	 * @return
	 */
	RequirementBoundTestCasesStatistics gatherBoundTestCaseStatistics(Collection<Long> requirementIds);
	
	/**
	 * Returns all of the above bundled in one bean. 
	 * 
	 * @param requirementIds
	 * @return
	 */
	RequirementStatisticsBundle gatherRequirementStatisticsBundle(Collection<Long> requirementIds);
	
	/**
	 * Given those requirements ids, sort them by status and returns how many of each were found. Warning : no security check will 
	 * be performed and the data will be returned regardless of who requested it.
	 * 
	 * @param requirementIds
	 * @return
	 */
	RequirementStatusesStatistics gatherRequirementStatusesStatistics(Collection<Long> requirementIds);
	/**
	 * Given those requirements ids, sort them by criticality and returns how many of each were found. Warning : no security check will 
	 * be performed and the data will be returned regardless of who requested it.
	 * 
	 * @param requirementIds
	 * @return
	 */
	RequirementCriticalityStatistics gatherRequirementCriticalityStatistics(Collection<Long> requirementIds);
	/**
	 * Given those requirements ids, returns how many of them have a description and how many haven't. Warning : no security check will 
	 * be performed and the data will be returned regardless of who requested it.
	 * 
	 * @param requirementIds
	 * @return
	 */
	RequirementBoundDescriptionStatistics gatherRequirementBoundDescriptionStatistics(Collection<Long> requirementIds);
}