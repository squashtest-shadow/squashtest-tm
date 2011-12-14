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
package org.squashtest.csp.tm.domain.testcase;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;

@Entity
@PrimaryKeyJoinColumn(name = "TEST_STEP_ID")
public class ActionTestStep extends TestStep implements AttachmentHolder {
	@Lob
	@Basic(optional = false)
	private String action;

	@Lob
	private String expectedResult;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false)
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	public ActionTestStep() {
		super();
	}

	public ActionTestStep(String action, String expectedResult) {
		super();
		this.action = action;
		this.expectedResult = expectedResult;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@NotBlank
	public String getAction() {
		return action;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	@Override
	public ActionTestStep createCopy() {
		ActionTestStep newTestStep = new ActionTestStep();
		newTestStep.action = this.action;
		newTestStep.expectedResult = this.expectedResult;

		// copy the attachments
		for (Attachment tcAttach : this.getAttachmentList().getAllAttachments()) {
			Attachment clone = tcAttach.hardCopy();
			newTestStep.getAttachmentList().addAttachment(clone);
		}

		return newTestStep;
	}

	@Override
	public void accept(TestStepVisitor visitor) {
		visitor.visit(this);

	}

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	@Override
	public List<ExecutionStep> getExecutionStep(){
		List<ExecutionStep> returnList = new ArrayList<ExecutionStep>();
		ExecutionStep exec = new ExecutionStep(this);
		returnList.add(exec);
		return returnList;
	}

}
