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
package org.squashtest.csp.tm.internal.service;

import java.util.List;

import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

/**
 * The following specialization for NodeDeletionHandler, meant for the campaigns, will also handle the deletion of
 * campaign related entities like Iterations and Executions. Thus the contract of the interface NodeDeletionHandler is
 * extended for those entities.
 * 
 * First, some definitions :
 * 
 * <ul>
 * <li>Targeted deletion : the user designated one or several entities to be specifically deleted</li>
 * <li>Cascaded deletion : the entities that become deleted due to the cascade mechanisms (initiated by a targeted
 * deletion higher in the hierarchy</li>
 * </ul>
 * 
 * 
 * <b>In the context of a targeted deletion</b>, Iterations and Executions may require a preview of the deletion
 * operation just like the other library nodes, so they both a simulateIterationDeletion and simulateExecutionDeletion
 * methods, which both obey to the same specifications than {@link NodeDeletionHandler#simulateDeletion(List)}. However,
 * while Iterations are candidates to mass-deletion, Executions are always deleted one at a time. That's why
 * {@link #deleteExecution(Execution)} have a different contract to enforce this.
 * 
 * On the other hand, <b>in the context of a cascade deletion</b> both Iterations and Executions will be removed <i>en
 * masse</i>.
 * 
 * Further remarks :
 * 
 * The disassociation an IterationTestPlanItem from an Iteration is already handled by IterationTestPlanManager. However
 * the implementation of this interface should also take care of these entities in the context of a cascade removal.
 * 
 * Same remark goes for CampaignTestPlanItem.
 * 
 * @author bsiri
 * 
 */
public interface CampaignNodeDeletionHandler extends NodeDeletionHandler<CampaignLibraryNode, CampaignFolder> {

	/**
	 * that method should investigate the consequences of the deletion of the given iterations, and return a report
	 * about what will happen.
	 * 
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateIterationDeletion(List<Long> targetIds);

	/**
	 * that method should delete the iterations. It still takes care of non deletable iterations so the implementation
	 * should filter out the ids that can't be deleted.
	 * 
	 * 
	 * @param targetIds
	 * @return the list of the ids of the iterations actually deleted.
	 */
	List<Long> deleteIterations(List<Long> targetIds);

	/**
	 * that method should investigate the consequences of the deletion of the given executions, and return a report
	 * about what will happen.
	 * 
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateExecutionDeletion(Long execId);

	/**
	 * that method should delete the execution. It still takes care of non deletable executions so the implementation
	 * should abort if the execution can't be deleted.
	 * 
	 * 
	 * @param targetIds
	 * @throws RuntimeException
	 *             if the execution should not be deleted.
	 */
	void deleteExecution(Execution execution);

	/**
	 * that method should delete test suites, and remove its references in iteration and iteration test plan item
	 * 
	 * @param testSuites
	 * @return
	 */
	List<Long> deleteSuites(List<TestSuite> testSuites);

}
