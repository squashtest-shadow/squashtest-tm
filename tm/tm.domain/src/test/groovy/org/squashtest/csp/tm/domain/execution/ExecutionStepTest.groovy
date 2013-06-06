/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.csp.tm.domain.execution

import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.testcase.ActionTestStep

import spock.lang.Specification

class ExecutionStepTest extends Specification {
	def "Should create an execution step for an ActionTestStep"() {
		given:
		ActionTestStep actionStep = new ActionTestStep(action: "action", expectedResult: "expectedResult")
		Attachment attach1 = new Attachment("name")
		actionStep.attachmentList.addAttachment(attach1)
		when:
		ExecutionStep execStep = new ExecutionStep(actionStep, null)

		then:
		execStep.action == actionStep.action
		execStep.expectedResult == actionStep.expectedResult
		execStep.referencedTestStep == actionStep
		execStep.attachmentList.getAllAttachments().size() == 1
	}
}
