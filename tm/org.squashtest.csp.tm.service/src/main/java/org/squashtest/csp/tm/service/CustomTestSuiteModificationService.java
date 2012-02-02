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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;

@Transactional
public interface CustomTestSuiteModificationService {
	
	/**
	 * That method will update the name of the suite with newName, identified by suiteId. Will throw a {@link DuplicateNameException} 
	 * if the suite could not rename itself due to name already used by another suite from the same owning Iteration (as as spec of TestSuite).
	 * 
	 * @param suiteId
	 * @param newName
	 * @throws DuplicateNameException
	 */
	void rename(long suiteId, String newName) throws DuplicateNameException;
	
	
	/**
	 * <p>That method will attach several {@link IterationTestPlanItem} to the given TestSuite. As usual, they
	 * are identified using their Ids. Since a given item can be bound to at most one test suite, the item
	 * be deassociated from its former TestSuite.</p>
	 * 
	 * <p>The implementation must also check that all these entities all belong to the same iteration or throw an unchecked exception
	 * if not. TODO : define that exception.</p> 
	 * 
	 * @param suiteId
	 * @param itemTestPlanIds
	 */
	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")		
	void bindTestPlan(long suiteId, List<Long> itemTestPlanIds);
	
}
