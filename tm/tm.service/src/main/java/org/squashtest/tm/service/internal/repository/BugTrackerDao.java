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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.internal.bugtracker.Pair;

public interface BugTrackerDao extends JpaRepository<BugTracker, Long> {


	/**
	 * @return number of all bugtrackers in squash database
	 */
	@NativeMethodFromJpaRepository
	long count();

	/**
	 * @return a page of bugtrackers according to the filter
	 */
	@UsesTheSpringJpaDsl
	Page<BugTracker> findAll(Pageable pageable);


	/**
	 *
	 * @return the list of distinct BugTrackers concerned by the given projects;
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<BugTracker> findDistinctBugTrackersForProjects(@Param("projects") List<Long> projectIds);

	/**
	 * Given its name, returns a bugtracker
	 */
	@UsesTheSpringJpaDsl
	BugTracker findByName(String bugtrackerName);

	/**
	 *
	 * @return the bugtracker bound to the campaign's project
     */
	@UsesANamedQueryInPackageInfoOrElsewhere
	BugTracker findByCampaignLibraryNode(@Param("node") CampaignLibraryNode node);

	/**
	 *
	 * @return the bugtracker bound to the excution's project
     */
	@UsesANamedQueryInPackageInfoOrElsewhere
	BugTracker findByExecution(@Param("execution") Execution execution);

	/**
	 *
     * @return the bugtracker bound to the iteration's project
     */
	@UsesANamedQueryInPackageInfoOrElsewhere
	BugTracker findByIteration(@Param("iteration") Iteration iteration);

	@UsesANamedQueryInPackageInfoOrElsewhere
	BugTracker findByTestSuite(@Param("testSuite") TestSuite testSuite);

	@UsesANamedQueryInPackageInfoOrElsewhere
	List<Pair<Execution, BugTracker>> findAllPairsByExecutions(@Param("executions") Collection<Execution> executions);

	@UsesANamedQueryInPackageInfoOrElsewhere
	BugTracker findByExecutionStep(@Param("step") ExecutionStep executionStep);
}
