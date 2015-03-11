/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.testcase;

import java.util.Collection;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;

@Transactional
@DynamicManager(name="squashtest.tm.service.TestCaseModificationService", entity = TestCase.class)
public interface TestCaseModificationService extends CustomTestCaseModificationService, TestCaseFinder {
	/**
	 * 
	 */
	static final String TEST_CASE_IS_WRITABLE = "hasPermission(#arg0, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')";

	@PreAuthorize(TEST_CASE_IS_WRITABLE)
	void changeDescription(long testCaseId, String newDescription);

	@PreAuthorize(TEST_CASE_IS_WRITABLE)
	void changeReference(long testCaseId, String reference);

	@PreAuthorize(TEST_CASE_IS_WRITABLE)
	void changeImportance(long testCaseId, TestCaseImportance importance);

	@PreAuthorize(TEST_CASE_IS_WRITABLE)
	void changeStatus(long testCaseId, TestCaseStatus status);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	void changePrerequisite(long testCaseId, String newPrerequisite);




}
