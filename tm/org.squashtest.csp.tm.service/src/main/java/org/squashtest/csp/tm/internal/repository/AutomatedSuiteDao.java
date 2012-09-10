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
import java.util.List;

import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;


public interface AutomatedSuiteDao{


	public AutomatedSuite createNewSuite();
	
	public AutomatedSuite findById(String id);
	
	
	public List<AutomatedSuite> findAll();
	
	
	public List<AutomatedSuite> findAllById(final Collection<String> ids);
	
	/**
	 * retrieve all the {@link AutomatedExecutionExtender} that this suite is bound to.
	 * 
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllExtenders(String suiteId);
	 
	/**
	 * retrieve all the extenders of executions currently waiting to be run by their test automation servers, for a given {@link AutomatedSuite}
	 * 
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllWaitingExtenders(String suiteId);
	
	/**
	 * retrieve all the extenders of executions currently being run by their test automation servers, for a given {@link AutomatedSuite}
	 * 
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllRunningExtenders(String suiteId);
	
	/**
	 * retrieve all the extenders of executions which had been ran their test automation servers, for a given {@link AutomatedSuite}
	 * 
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllCompletedExtenders(String suiteId);
	
	/**
	 * retrieve all the extenders of executions which status is one of the supplied status, for a given {@link AutomatedSuite}
	 * 
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllExtendersByStatus(String suiteId, Collection<ExecutionStatus> statusList);
	
}
