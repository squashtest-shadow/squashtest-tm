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
package org.squashtest.tm.service.statistics.requirement;

import java.util.Collection;

public class RequirementStatisticsBundle {

	private RequirementBoundTestCasesStatistics boundTestCasesStatistics;
	private RequirementStatusesStatistics statusesStatistics;
	private RequirementCriticalityStatistics criticalityStatistics;
	private RequirementBoundDescriptionStatistics boundDescriptionStatistics;
	private Collection<Long> selectedIds;
	
	public RequirementStatisticsBundle(RequirementBoundTestCasesStatistics boundTestCasesStatistics,
			RequirementStatusesStatistics statusesStatistics, RequirementCriticalityStatistics criticalityStatistics,
			RequirementBoundDescriptionStatistics boundDescriptionStatistics, Collection<Long> selectedIds) {
		super();
		this.boundTestCasesStatistics = boundTestCasesStatistics;
		this.statusesStatistics = statusesStatistics;
		this.criticalityStatistics = criticalityStatistics;
		this.boundDescriptionStatistics = boundDescriptionStatistics;
		this.selectedIds = selectedIds;
	}
	public RequirementStatisticsBundle() {
		super();
	}
	
	public Collection<Long> getSelectedIds() {
		return selectedIds;
	}
	public void setSelectedIds(Collection<Long> selectedIds) {
		this.selectedIds = selectedIds;
	}
	
	public RequirementBoundTestCasesStatistics getBoundTestCasesStatistics() {
		return boundTestCasesStatistics;
	}
	public void setBoundTestCasesStatistics(RequirementBoundTestCasesStatistics boundTestCasesStatistics) {
		this.boundTestCasesStatistics = boundTestCasesStatistics;
	}

	public RequirementStatusesStatistics getStatusesStatistics() {
		return statusesStatistics;
	}
	public void setStatusesStatistics(RequirementStatusesStatistics statusesStatistics) {
		this.statusesStatistics = statusesStatistics;
	}
	
	public RequirementCriticalityStatistics getCriticalityStatistics() {
		return criticalityStatistics;
	}
	public void setCriticalityStatistics(RequirementCriticalityStatistics criticalityStatistics) {
		this.criticalityStatistics = criticalityStatistics;
	}
	
	public RequirementBoundDescriptionStatistics getBoundDescriptionStatistics() {
		return boundDescriptionStatistics;
	}
	public void setBoundDescriptionStatistics(RequirementBoundDescriptionStatistics descriptionStatistics) {
		this.boundDescriptionStatistics = descriptionStatistics;
	}
	
}
