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

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.testcase.TestCase;

/**
 * @author Gregory Fouquet
 * 
 */
@Transactional
public interface CustomRequirementModificationService {
	@Transactional(readOnly = true)
	Requirement findById(long reqId);

	void rename(long reqId, String newName);

	/**
	 * @param requirementId
	 * @param pagingAndSorting
	 * @return
	 */
	@Transactional(readOnly = true)
	PagedCollectionHolder<List<TestCase>> findVerifyingTestCasesByRequirementId(long requirementId,
			PagingAndSorting pagingAndSorting);

	/**
	 * Increase the current version of the given requirement.
	 * 
	 * @param requirementId
	 */
	void createNewVersion(long requirementId);

}