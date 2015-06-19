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
package org.squashtest.tm.service.internal.batchimport

import org.junit.Test
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseStatus
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.internal.batchimport.Model.Existence
import org.squashtest.tm.service.internal.batchimport.Model.ProjectTargetStatus
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus
import org.squashtest.tm.service.internal.repository.UserDao
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.user.UserAccountService

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class ValidationFacilityTest extends Specification {
	ValidationFacility facility = new ValidationFacility()
	EntityValidator entityValidator = Mock()
	Model model = Mock()
	UserAccountService userAccount = Mock()
	PermissionEvaluationService permissionEvaluation = Mock()
	UserDao userDao = Mock()
	TargetStatus status = Mock()

	def setup() {
		facility.model = model
		facility.userAccountService = userAccount
		facility.permissionService = permissionEvaluation
		facility.userDao = userDao
		facility.entityValidator = entityValidator

		model.getStatus(_) >> status
		model.getTestCaseCufs(_) >> Collections.emptyList()
		model.getProjectStatus(_) >> Mock(ProjectTargetStatus)

		userAccount.findCurrentUser() >> Mock(User)
	}

	@Unroll
	def "should validate new test case with inconsistent path #path and name #name"() {
		given:
		LogTrain logTrain = new LogTrain()
		entityValidator.createTestCaseChecks(_, _) >> logTrain

		and:
		TestCaseTarget target = Mock()
		target.path >> path

		and:
		TestCase testCase = Mock()
		testCase.name >> name

		and:
		TestCaseInstruction instr = new TestCaseInstruction(target, testCase);

		and:
		status.status >> Existence.NOT_EXISTS

		when:
		LogTrain createLog = facility.createTestCase(instr)

		then:
		!createLog.criticalErrors

		where:
		path					| name
		"/the/path/is/straight"	| "deviant"
		"/the/path/is/straight"	| ""
	}

	def "should not validate old test case with inconsistent path and name"() {
		given:
		LogTrain logTrain = new LogTrain()
		entityValidator.updateTestCaseChecks(_, _) >> logTrain

		and:
		TestCaseTarget target = Mock()
		target.path >> "/the/path/is/straight"

		and:
		TestCase testCase = Mock()
		testCase.name >> "deviant"

		and:
		TestCaseInstruction instr = new TestCaseInstruction(target, testCase);

		and:
		status.status >> Existence.EXISTS

		when:
		LogTrain createLog = facility.updateTestCase(instr)

		then:
		createLog.criticalErrors
	}

	@Unroll
	def "should validate old test case with without name '#name'"() {
		given:
		LogTrain logTrain = new LogTrain()
		entityValidator.updateTestCaseChecks(_, _) >> logTrain

		and:
		TestCaseTarget target = Mock()
		target.path >> path

		and:
		TestCase testCase = Mock()
		testCase.name >> name

		and:
		TestCaseInstruction instr = new TestCaseInstruction(target, testCase);

		and:
		status.status >> Existence.EXISTS

		when:
		LogTrain createLog = facility.updateTestCase(instr)

		then:
		createLog.criticalErrors == fails

		where:
		path					| name		| fails
		"/the/path/is/straight"	| ""		| false
		"/the/path/is/straight"	| null		| false
	}
}
