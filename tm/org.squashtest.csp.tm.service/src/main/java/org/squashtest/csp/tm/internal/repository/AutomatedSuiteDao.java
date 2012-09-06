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
package org.squashtest.csp.tm.internal.repository;

import java.util.Collection;

import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.testautomation.AutomatedExecution;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;


public interface AutomatedSuiteDao {

	/**
	 * @return a new {@link AutomatedSuite}
	 */
	AutomatedSuite createNewSuite();
	
	/**
	 * 
	 * @param suiteId is the id of the suite to retrieve
	 * @return the TestAutomationSuite having this id if found, null if not.
	 */
	AutomatedSuite findById(long suiteId);
	
	
	/**
	 * retrieve all the {@link AutomatedExecution} that this suite is bound to.
	 * 
	 * @param suiteId
	 * @return
	 */
	Collection<AutomatedExecution> findAllExecutions(long suiteId);
	 
	/**
	 * retrieve all executions currently waiting to be run by their test automation servers, for a given {@link AutomatedSuite}
	 * 
	 * @param suiteId
	 * @return
	 */
	Collection<AutomatedExecution> findAllWaitingExecution(long suiteId);
	
	/**
	 * retrieve all executions currently being run by their test automation servers, for a given {@link AutomatedSuite}
	 * 
	 * @param suiteId
	 * @return
	 */
	Collection<AutomatedExecution> findAllRunningExecution(long suiteId);
	
	/**
	 * retrieve all executions which had been ran their test automation servers, for a given {@link AutomatedSuite}
	 * 
	 * @param suiteId
	 * @return
	 */
	Collection<AutomatedExecution> findAllTreatedExecution(long suiteId);
	
	/**
	 * retrieve all executions which status is one of the supplied status, for a given {@link AutomatedSuite}
	 * 
	 * @param suiteId
	 * @return
	 */
	Collection<AutomatedExecution> findAllByStatus(long suiteId, Collection<ExecutionStatus> statusList);
}
