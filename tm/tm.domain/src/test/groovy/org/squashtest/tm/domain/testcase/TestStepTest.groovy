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
package org.squashtest.tm.domain.testcase;

import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.testcase.ActionTestStep;

import spock.lang.Specification


class TestStepTest extends Specification {
	def "new step should not have attachments"() {
		when:
		ActionTestStep testStep = new ActionTestStep()


		then:
		!testStep.attachmentList.hasAttachments()
	}

	def "step should say it has attachments"() {
		given:
		ActionTestStep testStep = new ActionTestStep()

		when:
		Attachment att = new Attachment()
		testStep.getAttachmentList().addAttachment att

		then:
		testStep.attachmentList.hasAttachments()
	}
}
