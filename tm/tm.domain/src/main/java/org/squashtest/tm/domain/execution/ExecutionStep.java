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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.persistence.Transient;

import org.hibernate.annotations.Formula;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueList;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.library.HasExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Entity
@Auditable
public class ExecutionStep implements AttachmentHolder, IssueDetector, TestStepVisitor, Identified, HasExecutionStatus, DenormalizedFieldHolder, BoundEntity {
	
	private static final Set<ExecutionStatus> LEGAL_EXEC_STATUS;
	
	private static final String PARAM_PREFIX = "\\Q${\\E";
	private static final String PARAM_SUFFIX = "\\Q}\\E";
	private static final String PARAM_PATTERN = PARAM_PREFIX + "([A-Za-z0-9_-]+)" + PARAM_SUFFIX;
	private static final String NO_PARAM = "&lt;no_value&gt;";
	static {
		Set<ExecutionStatus> set = new HashSet<ExecutionStatus>();
		set.add(ExecutionStatus.UNTESTABLE);
		set.add(ExecutionStatus.SUCCESS);
		set.add(ExecutionStatus.BLOCKED);
		set.add(ExecutionStatus.FAILURE);
		set.add(ExecutionStatus.READY);
		LEGAL_EXEC_STATUS = Collections.unmodifiableSet(set);		
	}
	
	
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
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
	@JoinColumn(name = "ISSUE_LIST_ID")
	private IssueList issueList = new IssueList();

	@Transient
	private Map<String, String> dataset = new HashMap<String,String>();
	
	/**
	 * Hibernate needs this.
	 */
	protected ExecutionStep() {
		super();
	}

	public ExecutionStep(ActionTestStep testStep){
		this(testStep, null);
	}
	
	public ExecutionStep(ActionTestStep testStep, Dataset dataset) {
		fillParameterMap(dataset); 
		testStep.accept(this);
		referencedTestStep = testStep;
		for (Attachment actionStepAttach : testStep.getAllAttachments()) {
			Attachment clone = actionStepAttach.hardCopy();
			attachmentList.addAttachment(clone);
		}
	}
	
	private void fillParameterMap(Dataset dataset){
		if(dataset != null){
			for(DatasetParamValue param : dataset.getParameterValues()){
				String key = param.getParameter().getName();
				String value = param.getParamValue();
				this.dataset.put(key, value);
			}
		}
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

	@Override
	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}
	
	@Override
	public Set<ExecutionStatus> getLegalStatusSet() {
		return LEGAL_EXEC_STATUS;
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
	
	public boolean isFirst(){
		return (executionStepOrder==0);
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


	@AclConstrainedObject
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
	public void detachIssue(Long id){
		issueList.removeIssue(id);
	}
	
	@Override
	public void visit(ActionTestStep visited) {
		String originalAction = visited.getAction();
		String originalExpectedResult = visited.getExpectedResult();
		action = valueParams(originalAction);
		expectedResult = valueParams(originalExpectedResult);
	}

	private String valueParams(String content){
		
		String result = null;
		
		if(content != null){
			
			StringBuilder builder = new StringBuilder(content);
			
		    Pattern pattern = Pattern.compile(PARAM_PATTERN);
		    Matcher matcher = pattern.matcher(content);		
		    
		    // each time a replacement occurs in the stringbuilder deviates 
		    // a bit further from string scanned by the matcher.
		    //
		    // Consequently the more the string is modified the more the length might be altered,
		    // which leads to inconsistencies in the position of a given substring in the original string
		    // and the modified string.
		    //
		    // Thus, the following variable keeps track
		    // of the modifications to adjust the start and end position 
		    // 
		    int offset = 0;
			
		    while (matcher.find()){
		    	String paramName = matcher.group(1);
		    	
		    	String paramValue = dataset.get(paramName);		    	
		    	if( paramValue == null|| paramValue.length() == 0) {
		    		paramValue = NO_PARAM;
		    	}
		    	
		    	int start = matcher.start();
		    	int end = matcher.end();

		    	builder.replace(start + offset, end + offset, paramValue);
		    	
		    	offset += paramValue.length() - (end - start);	

		    }
		    
		    result = builder.toString();
		}
		else{
			result = content;
		}
	    return result;


	}
	
	@Override
	public void visit(CallTestStep visited) {
		// FIXME naive implementation so that app don't break
		action = visited.getCalledTestCase().getName();
	}
	
	@Override
		public List<Long> getAllIssueListId() {
			List<Long> ids = new LinkedList<Long>();
			ids.add(issueList.getId());
			return ids;
		}

	@Override
	public BugTracker getBugTracker() {
		return getProject().findBugTracker();
	}

	@Override
	public Long getDenormalizedFieldHolderId() {
		return getId();
	}

	@Override
	public DenormalizedFieldHolderType getDenormalizedFieldHolderType() {
		return DenormalizedFieldHolderType.EXECUTION_STEP;
	}
	
	// ***************** (detached) custom field section *************

	@Override
	public Long getBoundEntityId() {
		return getId();
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.EXECUTION_STEP;
	}
}
