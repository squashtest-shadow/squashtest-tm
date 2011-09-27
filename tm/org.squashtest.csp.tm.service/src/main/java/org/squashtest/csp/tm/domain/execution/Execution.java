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
package org.squashtest.csp.tm.domain.execution;

import java.util.ArrayList;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Formula;
import org.squashtest.csp.tm.domain.attachment.Attachable;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.IssueList;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;

@Auditable
@Entity
public class Execution implements Attachable, Bugged {

	@Id
	@GeneratedValue
	@Column(name = "EXECUTION_ID")
	private Long id;

	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.READY;

	@Enumerated(EnumType.STRING)
	private TestCaseExecutionMode executionMode = TestCaseExecutionMode.MANUAL;

	@Lob
	private String description;

	@Basic(optional = false)
	private String name;

	@ManyToOne
	@JoinTable(name="ITEM_TEST_PLAN_EXECUTION",joinColumns =  @JoinColumn(name = "EXECUTION_ID"), inverseJoinColumns = @JoinColumn(name = "ITEM_TEST_PLAN_ID"))
	private IterationTestPlanItem testPlan;


	@ManyToOne
	@JoinColumn(name = "TCLN_ID", referencedColumnName = "TCLN_ID")
	private TestCase referencedTestCase;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "EXECUTION_STEP_ORDER")
	@JoinTable(name = "EXECUTION_EXECUTION_STEPS", joinColumns = @JoinColumn(name = "EXECUTION_ID"), inverseJoinColumns = @JoinColumn(name = "EXECUTION_STEP_ID"))
	private final List<ExecutionStep> steps = new ArrayList<ExecutionStep>();

	@Formula("(select ITEM_TEST_PLAN_EXECUTION.EXECUTION_ORDER from ITEM_TEST_PLAN_EXECUTION where ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID = EXECUTION_ID)")
	private Integer executionOrder;

	@Column(insertable = false)
	private String lastExecutedBy;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastExecutedOn;

	/* *********************** attachment attributes ************************ */

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE } )
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentCollection = new AttachmentList();
	/* ***********************   / attachement attributes ************************ */


	/* *********************** issues attributes ************************ */

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE } )
	@JoinColumn(name = "ISSUE_LIST_ID")
	private final IssueList issueList = new IssueList();

	/* *********************** /issues attributes ************************ */


	public List<ExecutionStep> getSteps() {
		return steps;
	}

	public Execution() {

	}

	public Execution(String description) {
		this.description = description;
	}

	public Execution(String description, TestCase testCase) {
		this(description);
		setReferencedTestCase(testCase);
	}

	public Execution(TestCase testCase) {
		setReferencedTestCase(testCase);
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus status) {
		executionStatus = status;
		//update parentTestPlan status

		IterationTestPlanItem itp = getTestPlan();

		if (itp!=null){
			itp.updateExecutionStatus();
		}

	}

	public Integer getExecutionOrder() {
		return executionOrder;
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

	public final void setReferencedTestCase(TestCase testCase) {
		referencedTestCase = testCase;
		executionMode = testCase.getExecutionMode();
		setName(testCase.getName());
	}

	public TestCaseExecutionMode getExecutionMode() {
		return executionMode;
	}

	@Override
	public Long getId() {
		return id;
	}

	public TestCase getReferencedTestCase() {
		return referencedTestCase;
	}

	public String getName() {
		return this.name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addStep(@NotNull ExecutionStep step) {
		steps.add(step);
	}


	/* *************** Attachable implementation ****************** */

	@Override
	public Long getAttachmentCollectionId() {
		return attachmentCollection.getId();
	}


	@Override
	public AttachmentList getAttachmentCollection() {
		return attachmentCollection;
	}

	@Override
	public boolean hasAttachments(){
		return attachmentCollection.hasAttachments();
	}

	@Override
	public int getNbAttachments() {
		return getAttachmentCollection().size();
	}


	public IterationTestPlanItem getTestPlan(){
		return testPlan;
	}

	public void setTestPlan(IterationTestPlanItem testPlan){
		this.testPlan=testPlan;
	}

	/* ***************** Bugged implementation *********************** */
	@Override
	public Project getProject(){
		return testPlan.getProject();
	}

	@Override
	public IssueList getIssueList() {
		return issueList;
	}

	@Override
	public Long getIssueListId() {
		return issueList.getId();
	}

	@Override
	public List<Long> getAllIssueListId() {
		List<Long> list = new LinkedList<Long>();

		list.add(issueList.getId());

		for (ExecutionStep step : steps){
			list.addAll(step.getAllIssueListId());
		}

		return list;
	}

	@Override
	public String getDefaultDescription() {
		return "";
	}


	@Override
	public List<Bugged> getAllBuggeds() {
		List<Bugged> list = new LinkedList<Bugged>();

		list.add(this);

		for (ExecutionStep step : steps){
			list.addAll(step.getAllBuggeds());
		}

		return list;
	}

}
