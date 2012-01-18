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


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;

@Transactional
public interface TestCaseModificationService extends CustomTestCaseModificationService {
	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeDescription(long testCaseId, String newDescription);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeExecutionMode(long testCaseId, TestCaseExecutionMode mode);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeImportance(long testCaseId, TestCaseImportance importance);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	void changeImportanceAuto(long testCaseId, boolean auto);

}
