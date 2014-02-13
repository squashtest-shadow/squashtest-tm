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
package org.squashtest.tm.service.internal.execution;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.execution.ExecutionModificationService;
import org.squashtest.tm.service.internal.campaign.CampaignNodeDeletionHandler;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
import org.squashtest.tm.service.library.AdvancedSearchService;

@Service("squashtest.tm.service.ExecutionModificationService")
public class ExecutionModificationServiceImpl implements ExecutionModificationService {

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Inject 
	private AdvancedSearchService advancedSearchService;
	
	@Override
	public Execution findAndInitExecution(Long executionId) {
		return executionDao.findAndInit(executionId);
	}

	@Override
	@PreAuthorize("hasPermission(#executionId, 'org.squashtest.tm.domain.execution.Execution', 'EXECUTE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void setExecutionDescription(Long executionId, String description) {
		Execution execution = executionDao.findById(executionId);
		execution.setDescription(description);
	}

	@Override
	public List<ExecutionStep> findExecutionSteps(long executionId) {
		return executionDao.findExecutionSteps(executionId);
	}

	@Override
	public int findExecutionRank(Long executionId) {
		return executionDao.findExecutionRank(executionId);
	}

	@Override
	@PreAuthorize("hasPermission(#executionStepId, 'org.squashtest.tm.domain.execution.ExecutionStep', 'EXECUTE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void setExecutionStepComment(Long executionStepId, String comment) {
		ExecutionStep executionStep = executionStepDao.findById(executionStepId);
		executionStep.setComment(comment);
	}

	@Override
	public PagedCollectionHolder<List<ExecutionStep>> findExecutionSteps(long executionId, Paging filter) {
		List<ExecutionStep> list = executionDao.findStepsFiltered(executionId, filter);
		long count = executionDao.countExecutionSteps(executionId);
		return new PagingBackedPagedCollectionHolder<List<ExecutionStep>>(filter, count, list);
	}

	@Override
	@PreAuthorize("hasPermission(#execId, 'org.squashtest.tm.domain.execution.Execution', 'EXECUTE') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<SuppressionPreviewReport> simulateExecutionDeletion(Long execId) {
		return deletionHandler.simulateExecutionDeletion(execId);
	}

	@Override
	@PreAuthorize("hasPermission(#execution, 'EXECUTE') "
			+	"or hasRole('ROLE_ADMIN')")
	public void deleteExecution(Execution execution) {
		TestCase testCase = execution.getReferencedTestCase();
		deletionHandler.deleteExecution(execution);
		if (testCase != null){
			advancedSearchService.reindexTestCase(testCase.getId());
		}
	}

	@Override
	public Execution findById(long id) {
		return executionDao.findById(id);
	}

	@Override
	public ExecutionStep findExecutionStepById(long id) {
		return executionStepDao.findById(id);
	}

	/**
	 * @see org.squashtest.tm.service.execution.ExecutionFinder#findAllByTestCaseIdOrderByRunDate(long, org.squashtest.csp.core.infrastructure.collection.Paging)
	 *      org.squashtest.csp.core.infrastructure.collection.Paging)
	 */
	@Override
	public List<Execution> findAllByTestCaseIdOrderByRunDate(long testCaseId, Paging paging) {
		return executionDao.findAllByTestCaseIdOrderByRunDate(testCaseId, paging);
	}

	@Override
	public PagedCollectionHolder<List<Execution>> findAllByTestCaseId(long testCaseId, PagingAndSorting pas) {
		List<Execution> executions = executionDao.findAllByTestCaseId(testCaseId, pas);
		long count = executionDao.countByTestCaseId(testCaseId);
		return new PagingBackedPagedCollectionHolder<List<Execution>>(pas, count, executions);
	}

}
