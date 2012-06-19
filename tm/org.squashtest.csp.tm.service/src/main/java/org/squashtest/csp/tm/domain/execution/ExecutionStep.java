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
package org.squashtest.csp.tm.domain.execution;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Formula;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.IssueList;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.CallTestStep;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.domain.testcase.TestStepVisitor;

@Entity
@Auditable
public class ExecutionStep implements AttachmentHolder, Bugged, TestStepVisitor {
	@Id
	@GeneratedValue
	@Column(name = "EXECUTION_STEP_ID")
	private Long id;

	@Lob
	@Basic(optional = false)
	private String action;

	@Lob
	private String expectedResult;

	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.READY;

	@Lob
	private String comment;

	@Column(insertable = false)
	private String lastExecutedBy;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastExecutedOn;

	@ManyToOne()
	@JoinColumn(name = "TEST_STEP_ID")
	private TestStep referencedTestStep;

	@ManyToOne
	@JoinTable(name = "EXECUTION_EXECUTION_STEPS", joinColumns = @JoinColumn(name = "EXECUTION_STEP_ID", insertable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "EXECUTION_ID", insertable = false, updatable = false))
	private Execution execution;

	@Formula("(select EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ORDER from  EXECUTION_EXECUTION_STEPS where  EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID = EXECUTION_STEP_ID)")
	private Integer executionStepOrder;

	/* attachment attributes */
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	/* issues attributes */

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ISSUE_LIST_ID")
	private final IssueList issueList = new IssueList();

	/**
	 * Hibernate needs this.
	 */
	protected ExecutionStep() {
		super();
	}

	public ExecutionStep(TestStep testStep) {
		testStep.accept(this);
		referencedTestStep = testStep;
	}

	public TestStep getReferencedTestStep() {
		return referencedTestStep;
	}

	public Execution getExecution() {
		return execution;
	}

	public Integer getExecutionStepOrder() {
		return executionStepOrder;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getLastExecutedBy() {
		return lastExecutedBy;
	}

	public void setLastExecutedBy(String lastExecutedBy) {
		this.lastExecutedBy = lastExecutedBy;
	}

	public Date getLastExecutedOn() {
		return lastExecutedOn;
	}

	public void setLastExecutedOn(Date lastExecutedOn) {
		this.lastExecutedOn = lastExecutedOn;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	/* ********************* interface Attachable impl ****************** */

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	/* *** issues code *** */

	@Override
	public Long getIssueListId() {
		return issueList.getId();
	}

	/* I know the following is not nice */
	// TODO So find a way to remove it !! visitor, tagfile helper, whatever
	@Override
	public String getDefaultDescription() {
		StringBuilder builder = new StringBuilder();
		builder.append("<b>Action :</b>\n").append(action).append("\n\n");
		builder.append("<b>Expected Result :</b>\n").append(expectedResult).append("\n\n");
		return builder.toString();
	}

	@Override
	public CampaignLibrary getCampaignLibrary() {
		return execution.getCampaignLibrary();
	}

	@Override
	public Project getProject() {
		return execution.getProject();
	}

	@Override
	public IssueList getIssueList() {
		return issueList;
	}

	@Override
	public List<Long> getAllIssueListId() {
		List<Long> ids = new LinkedList<Long>();
		ids.add(issueList.getId());
		return ids;
	}

	@Override
	public List<Bugged> getAllBuggeds() {
		List<Bugged> list = new LinkedList<Bugged>();
		list.add(this);
		return list;
	}

	@Override
	public void visit(ActionTestStep visited) {
		action = visited.getAction();
		expectedResult = visited.getExpectedResult();
	}

	@Override
	public void visit(CallTestStep visited) {
		// FIXME naive implementation so that app don't break
		action = visited.getCalledTestCase().getName();
	}
	
	@Override
	public boolean isAcceptsIssues() {
		return true;
	}

}
