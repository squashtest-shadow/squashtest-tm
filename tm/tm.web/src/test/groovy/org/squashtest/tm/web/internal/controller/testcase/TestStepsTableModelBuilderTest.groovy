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
package org.squashtest.tm.web.internal.controller.testcase

import org.apache.poi.hssf.record.formula.functions.T
import org.springframework.context.MessageSource
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.CallTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.domain.testcase.TestStep

import spock.lang.Specification

class TestStepsTableModelBuilderTest extends Specification {
	MessageSource messageSource = Mock()
	Locale locale = Locale.FRENCH
	TestStepsTableModelBuilder builder = new TestStepsTableModelBuilder(messageSource, locale)

	def "Should build model for an ActionTestStep"() {
		given:
		ActionTestStep step = new ActionTestStep(action: "action", expectedResult: "expected")
		int stepIndex = 0

		use(ReflectionCategory) {
			TestStep.set field: 'id', of: step, to: 10L
			AttachmentList.set field: 'id', of: step.attachmentList, to: 100L
		}

		when:
		def data = builder.buildItemData(step);

		then:
		data == [
				 "step-id" : step.id, 
				 "step-index" : stepIndex, 
				 "nb-attachments" : step.attachmentList.size(),
				 "attach-list-id" : step.attachmentList.id,
				 "step-action" : "action", 
				 "step-result" : step.expectedResult,
				 "step-type" : "action",
				 "called-tc-id" : null,
				 "empty-delete-holder" : null,
				 "empty-browse-holder" : null,
				 "customFields" : [:]
			]	
	}

	def "Should build model for a CallTestStep"() {
		given:
		TestCase callee = new TestCase(name: "callee")
		CallTestStep step = new CallTestStep(calledTestCase: callee)
		int stepIndex = 0

		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: callee, to: 100L
			TestStep.set field: 'id', of: step, to: 10L
		}

		and:
		messageSource.getMessage("test-case.call-step.action.template", ["callee"], _) >> "Call : callee"

		when:
		def data = builder.buildItemData(step);

		then:
		
		data == [
			"step-id" : step.id,
			"step-index" : stepIndex,
			"nb-attachments" : null,
			"attach-list-id" : null,
			"step-action" : "Call : callee",
			"step-result" : null,
			"step-type" : "call",
			"called-tc-id" : callee.id,
			"empty-delete-holder" : null,
			"empty-browse-holder" : null,
			"customFields" : [:]
	   ]
	
	}
}
