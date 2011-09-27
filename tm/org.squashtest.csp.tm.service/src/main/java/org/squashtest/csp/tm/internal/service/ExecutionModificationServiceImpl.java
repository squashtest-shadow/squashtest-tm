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

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.squashtest.csp.tm.internal.repository.ExecutionStepDao;
import org.squashtest.csp.tm.service.ExecutionModificationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

/*
 * //FIXME : see ci.squashtest.org, task #105
 * 
 * 
 */


@Service("squashtest.tm.service.ExecutionModificationService")
public class ExecutionModificationServiceImpl implements
		ExecutionModificationService {
	
	@Inject
	private ExecutionDao executionDao;
	
	@Inject 
	private ExecutionStepDao executionStepDao;
	
	@Inject 
	private CampaignNodeDeletionHandler deletionHandler;


	@Override
	public Execution findExecution(Long executionId) {
		return executionDao.findAndInit(executionId);
	}

	@Override
	public Execution findAndInitExecution(Long executionId) {
		return executionDao.findAndInit(executionId);
	}

	@Override
	public void setExecutionDescription(Long executionId, String description) {
		Execution execution = executionDao.findById(executionId);
		execution.setDescription(description);
	}

	

	@Override
	public List<ExecutionStep> getExecutionSteps(Long executionId) {
		return executionDao.findExecutionSteps(executionId);
	}

	
	@Override
	public int findExecutionRank(Long executionId) {
		return executionDao.findExecutionRank(executionId);
	}
	
	@Override
	public void setExecutionStepComment(Long executionStepId, String comment){
		ExecutionStep executionStep = executionStepDao.findById(executionStepId);
		executionStep.setComment(comment);
	}

	@Override
	public FilteredCollectionHolder<List<ExecutionStep>> getExecutionSteps(
			long executionId, CollectionFilter filter) {
		List<ExecutionStep> list = executionDao.findStepsFiltered(executionId,
				filter);
		long count = getExecutionSteps(executionId).size();
		return new FilteredCollectionHolder<List<ExecutionStep>>(count, list);
	}

	@Override
	public List<SuppressionPreviewReport> simulateExecutionDeletion(Long execId) {
		return deletionHandler.simulateExecutionDeletion(execId);
	}

	@Override
	public void deleteExecution(Execution execution) {
		deletionHandler.deleteExecution(execution);
	}



}
