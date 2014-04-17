/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.domain.execution;

import static org.squashtest.tm.domain.testcase.TestCaseImportance.LOW;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import javax.validation.constraints.Size;

import org.hibernate.annotations.Formula;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueList;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.library.HasExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.exception.NotAutomatedException;
import org.squashtest.tm.exception.execution.ExecutionHasNoRunnableStepException;
import org.squashtest.tm.exception.execution.ExecutionHasNoStepsException;
import org.squashtest.tm.exception.execution.IllegalExecutionStatusException;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Auditable
@Entity
public class Execution implements AttachmentHolder, IssueDetector, Identified, HasExecutionStatus, DenormalizedFieldHolder, BoundEntity {
	
	static final Set<ExecutionStatus> LEGAL_EXEC_STATUS;
	
	static {
		Set<ExecutionStatus> set = new HashSet<ExecutionStatus>();
		set.add(ExecutionStatus.SUCCESS);
		set.add(ExecutionStatus.BLOCKED);
		set.add(ExecutionStatus.FAILURE);
		set.add(ExecutionStatus.RUNNING);
		set.add(ExecutionStatus.READY);
		set.add(ExecutionStatus.UNTESTABLE);
		set.add(ExecutionStatus.SETTLED);
		LEGAL_EXEC_STATUS = Collections.unmodifiableSet(set);
	}
	
	
	@Id
	@GeneratedValue
	@Column(name = "EXECUTION_ID")
	private Long id;

	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.READY;

	@Enumerated(EnumType.STRING)
	protected TestCaseExecutionMode executionMode = TestCaseExecutionMode.MANUAL;

	@Lob
	private String description;

	@Lob
	private String prerequisite = "";

	@NotNull
	private String reference = "";
	
	@Lob
	@Column(name = "TC_DESCRIPTION")
	private String tcdescription;
	
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	private TestCaseImportance importance = LOW;

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_NATURE")
	private TestCaseNature nature = TestCaseNature.UNDEFINED;
	
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_TYPE")
	private TestCaseType type = TestCaseType.UNDEFINED;

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_STATUS")
	private TestCaseStatus status = TestCaseStatus.WORK_IN_PROGRESS;
	
	@NotBlank
	@Size(min = 0, max = 255)
	private String name;

	// TODO rename as testPlanItem
	@ManyToOne
	@JoinTable(name = "ITEM_TEST_PLAN_EXECUTION", joinColumns = @JoinColumn(name = "EXECUTION_ID", insertable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "ITEM_TEST_PLAN_ID", insertable = false, updatable = false))
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
	

	@OneToOne(mappedBy="execution", cascade = { CascadeType.REMOVE, CascadeType.PERSIST}, optional = true)
	private AutomatedExecutionExtender automatedExecutionExtender;

	/* *********************** attachment attributes ************************ */

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();
	
	
	/* *********************** / attachement attributes ************************ */

	/* *********************** issues attributes ************************ */

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
	@JoinColumn(name = "ISSUE_LIST_ID")
	private IssueList issueList = new IssueList();

	/* *********************** /issues attributes ************************ */

	public List<ExecutionStep> getSteps() {
		return steps;
	}

	public Execution() {

	}

	/**
	 * Creates an execution for the test case references by the given tess plan item. Should be used by
	 * {@link IterationTestPlanItem} only.
	 * 
	 * @param testPlanItem
	 */
	public Execution(TestCase testCase) {
		this(testCase, new Dataset());
	}

	public Execution(TestCase testCase, Dataset dataset) {
		setReferencedTestCase(testCase);
		populateSteps(dataset);
		populateAttachments();
	}
	
	private void populateAttachments() {
		for (Attachment tcAttach : referencedTestCase.getAllAttachments()) {
			Attachment clone = tcAttach.hardCopy();
			attachmentList.addAttachment(clone);
		}
	}

	private void populateSteps(Dataset dataset) {
		for (TestStep step : referencedTestCase.getSteps()) {
			List<ExecutionStep> execList = step.createExecutionSteps(dataset);
			for (ExecutionStep executionStep : execList) {
				addStep(executionStep);
			}
		}
	}
	


	/* ******************** HasExecutionStatus implementation ************** */
	


	@Override
	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	
	
	/* ******************** /HasExecutionStatus implementation ************** */


	

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

	private void setReferencedTestCase(TestCase testCase) {
		referencedTestCase = testCase;

		if(testCase.getReference() != null && !testCase.getReference().equals("")){
			setName(testCase.getReference()+" - "+testCase.getName());
		} else {
			setName(testCase.getName());
		}
		
		nullSafeSetTestCaseData(testCase);
		setImportance(testCase.getImportance());
		setNature(testCase.getNature());
		setType(testCase.getType());
		setStatus(testCase.getStatus());

	}

	private void nullSafeSetTestCaseData(TestCase testCase) {
		// though it's constrained by the app, database allows null test case prerequisite or reference. hence this safety belt.
		
		String pr = testCase.getPrerequisite();
		setPrerequisite(pr == null ? "" : pr);
		
		pr = testCase.getReference();
		setReference(pr == null ? "" : pr);
		
		pr = testCase.getDescription();
		setTcdescription(pr == null ? "" : pr);
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

	public String getPrerequisite() {
		return prerequisite;
	}
	
	/**
	 * @param prerequisite
	 *            the prerequisite to set
	 */
	public void setPrerequisite(@NotNull String prerequisite) {
		this.prerequisite = prerequisite;
	}

	
	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public TestCaseImportance getImportance() {
		return importance;
	}

	public void setImportance(@NotNull TestCaseImportance importance) {
		this.importance = importance;
	}

	public TestCaseNature getNature() {
		return nature;
	}

	public void setNature(@NotNull TestCaseNature nature) {
		this.nature = nature;
	}

	public TestCaseType getType() {
		return type;
	}

	public void setType(@NotNull TestCaseType type) {
		this.type = type;
	}

	public TestCaseStatus getStatus() {
		return status;
	}

	public void setStatus(@NotNull TestCaseStatus status) {
		this.status = status;
	}
	
	public String getTcdescription() {
		return tcdescription;
	}

	public void setTcdescription(String tcdescription) {
		this.tcdescription = tcdescription;
	}

	private void addStep(@NotNull ExecutionStep step) {
		steps.add(step);
	}

	/**
	 * <p>
	 * return the first step with a running or a ready state.<br>
	 * Or null if there is none or the execution has no steps
	 * </p>
	 * 
	 * @return
	 */
	public ExecutionStep findFirstUnexecutedStep() {
		if (!this.getSteps().isEmpty()) {
			for (ExecutionStep step : this.getSteps()) {
				if (!step.getExecutionStatus().isTerminatedStatus()) {
					return step;
				}
			}
		}
		return null;
	}

	public boolean hasUnexecutedSteps() {
		return findFirstUnexecutedStep() != null;
	}
	

	/* *************** Attachable implementation ****************** */
	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	public IterationTestPlanItem getTestPlan() {
		return testPlan;
	}

	
	@AclConstrainedObject
	public CampaignLibrary getCampaignLibrary() {
		return testPlan.getProject().getCampaignLibrary();
	}
	
	/* ***************** Bugged implementation *********************** */
	@Override
	public Project getProject() {
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
	public void detachIssue(Long id){
		issueList.removeIssue(id);
	}
	
	/* ***************** /Bugged implementation *********************** */

	public void notifyAddedTo(IterationTestPlanItem testPlan) {
		this.testPlan = testPlan;
	}

	/**
	 * @return the first step not in success.
	 * @throws ExecutionHasNoStepsException
	 * @throws ExecutionHasNoRunnableStepException
	 */
	public ExecutionStep findFirstRunnableStep() throws ExecutionHasNoStepsException,
			ExecutionHasNoRunnableStepException {
		// Note : this was transplanted from untested HibernateExecDao method, I'm not sure of biz rules
		if (steps.isEmpty()) {
			throw new ExecutionHasNoStepsException();
		}

		for (ExecutionStep step : steps) {
			if (step.getExecutionStatus().isNoneOf(ExecutionStatus.SUCCESS)) {
				return step;
			}
		}

		throw new ExecutionHasNoRunnableStepException();
	}
	
	/**
	 * @return the last step of the execution.
	 * @throws ExecutionHasNoStepsException
	 *             if there are no steps
	 */
	public ExecutionStep getLastStep() throws ExecutionHasNoStepsException {
		if (steps.isEmpty()) {
			throw new ExecutionHasNoStepsException();
		}
		return steps.get(steps.size() - 1);
	}
	@Override
		public List<Long> getAllIssueListId() {
			List<Long> list = new LinkedList<Long>();
	
			list.add(issueList.getId());
	
			for (ExecutionStep step : steps) {
				list.addAll(step.getAllIssueListId());
			}
	
			return list;
		}

	@Override
	public BugTracker getBugTracker() {
		return getProject().findBugTracker();
	}

	
	/* ************************** test automation section (delegate to AutomatedExecutionExtender ) ************************* */

	public AutomatedExecutionExtender getAutomatedExecutionExtender() {
		return automatedExecutionExtender;
	}

	
	public void setAutomatedExecutionExtender(AutomatedExecutionExtender extender) {
		this.automatedExecutionExtender = extender;
		executionMode = TestCaseExecutionMode.AUTOMATED;
	}
	
	
	
	public boolean isAutomated(){
		return (executionMode == TestCaseExecutionMode.AUTOMATED && automatedExecutionExtender!=null);
	}
	
	private boolean checkValidNewStatus(ExecutionStatus status){
		if (isAutomated()){
			return (automatedExecutionExtender.getLegalStatusSet().contains(status));
		}
		else{
			return getLegalStatusSet().contains(status);
		}
	}
	
	
	public void setExecutionStatus(ExecutionStatus status) {
		
		if ( ! checkValidNewStatus(status)){
			throw new IllegalExecutionStatusException();
		}
		
		executionStatus = status;
		
		// update parentTestPlan status

		IterationTestPlanItem itp = getTestPlan();

		if (itp != null) {
			itp.updateExecutionStatus();
		}
	}
	
	@Override
	public Set<ExecutionStatus> getLegalStatusSet() {
		if (isAutomated()){
			return automatedExecutionExtender.getLegalStatusSet();
		}
		else{
			return LEGAL_EXEC_STATUS;
		}
	}
	
	public AutomatedTest getAutomatedTest(){
		if (isAutomated()){
			return automatedExecutionExtender.getAutomatedTest();
		}
		
		throw new NotAutomatedException();
	}

	public URL getResultURL(){
		if ( isAutomated()){
			return automatedExecutionExtender.getResultURL();
		}
		throw new NotAutomatedException();
	}
	
	public AutomatedSuite getAutomatedSuite(){
		if ( isAutomated()){
			return automatedExecutionExtender.getAutomatedSuite();
		}
		throw new NotAutomatedException();		
	}
	
	public String getResultSummary(){
		if ( isAutomated()){
			return automatedExecutionExtender.getResultSummary();
		}
		throw new NotAutomatedException();		
	}

	@Override
	public Long getDenormalizedFieldHolderId() {
		return getId();
	}

	@Override
	public DenormalizedFieldHolderType getDenormalizedFieldHolderType() {
		return DenormalizedFieldHolderType.EXECUTION;
	}

	/**
	 * returns the index of the step matching the given id or <code>-1</code> if step is not found.
	 * 
	 * @param stepId
	 * @return index of step or -1
	 */
	public int getStepIndex(long stepId) {
		for(ExecutionStep step : steps){
			if(step.getId() == stepId) {
				return steps.indexOf(step);
			}
		}
		return -1;
	}

	// ***************** (detached) custom field section *************

	@Override
	public Long getBoundEntityId() {
		return getId();
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.EXECUTION;
	}
	
	/**
	 * will compute from scratch a status using a complete report.
	 * 
	 * @param report
	 *            : ExecutionStatusReport.
	 * @return : ExecutionStatus.
	 * 
	 */
	public static ExecutionStatus computeNewStatus(ExecutionStatusReport report) {

		ExecutionStatus newStatus = ExecutionStatus.READY;

		if (report.has(ExecutionStatus.BLOCKED)) {
			newStatus = ExecutionStatus.BLOCKED;

		} else if (report.has(ExecutionStatus.FAILURE)) {
			newStatus = ExecutionStatus.FAILURE;

		} else if (report.allOf(ExecutionStatus.UNTESTABLE)) {
			newStatus =ExecutionStatus.UNTESTABLE;

		} else if (report.allOf(ExecutionStatus.SETTLED, ExecutionStatus.UNTESTABLE)) {
			newStatus = ExecutionStatus.SETTLED;
			
		} else if (report.allOf(ExecutionStatus.SUCCESS, ExecutionStatus.UNTESTABLE, ExecutionStatus.SETTLED)) {
			newStatus = ExecutionStatus.SUCCESS;

		} else if (report.anyOf(ExecutionStatus.SUCCESS, ExecutionStatus.SETTLED)) {
			newStatus = ExecutionStatus.RUNNING;

		}

		return newStatus;
	}

}
