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
package org.squashtest.csp.tm.domain.campaign;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.users.User;

@Entity
@Auditable
public class IterationTestPlanItem {
	@Id
	@GeneratedValue
	@Column(name = "ITEM_TEST_PLAN_ID")
	private Long id;

	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.READY;

	private String label = "";

	@ManyToOne
	@JoinColumn(name="USER_ID")
	private User user;

	@Column(insertable = false)
	private String lastExecutedBy;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastExecutedOn;

	@ManyToOne
	@JoinColumn(name = "TCLN_ID", referencedColumnName = "TCLN_ID")
	private TestCase referencedTestCase;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "EXECUTION_ORDER")
	@JoinTable(name = "ITEM_TEST_PLAN_EXECUTION", joinColumns = @JoinColumn(name = "ITEM_TEST_PLAN_ID"), inverseJoinColumns = @JoinColumn(name = "EXECUTION_ID"))
	private final List<Execution> executions = new ArrayList<Execution>();

	@ManyToOne
	@JoinTable(name="ITEM_TEST_PLAN_LIST",joinColumns =  @JoinColumn(name = "ITEM_TEST_PLAN_ID"), inverseJoinColumns = @JoinColumn(name = "ITERATION_ID"))
	private Iteration iteration;

	public IterationTestPlanItem() {
		super();
	}

	public Iteration getIteration(){
		return iteration;
	}

	public IterationTestPlanItem(TestCase testCase) {
		referencedTestCase = testCase;
		label = testCase.getName();
	}

	public IterationTestPlanItem(CampaignTestPlanItem campaignItem) {
		TestCase testCase = campaignItem.getReferencedTestCase();
		referencedTestCase = testCase;
		label = testCase.getName();
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}

	/**
	 * the IterationTestPlanItem will fetch the ExecutionStatus of the last "live" Execution in his execution list
	 *
	 */
	public void updateExecutionStatus() {
		int iIndexLastExec = executions.size();
		if (iIndexLastExec == 0) {
			return;
		}
		Execution execution = executions.get(iIndexLastExec - 1);
		executionStatus = execution.getExecutionStatus();
	}

	public TestCase getReferencedTestCase() {
		return referencedTestCase;
	}

	public void setReferencedTestCase(TestCase referencedTestCase) {
		this.referencedTestCase = referencedTestCase;
	}

	public Long getId() {
		return id;
	}

	public List<Execution> getExecutions() {
		return executions;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
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

	/**
	 * that method will also forward the information to the iteration for update of autoset dates.
	 */
	public void setLastExecutedOn(Date lastExecutedOn) {
		this.lastExecutedOn = lastExecutedOn;

		if (getIteration()!=null){
			getIteration().updateAutoDates(lastExecutedOn);
		}
	}

	public void emptyExecutions() {
		this.executions.clear();
	}

	public void addExecution(@NotNull Execution execution) {
		if (execution == null) {
			throw new IllegalArgumentException("IterationTestPlanItem : cannot insert null execution");
		}
		execution.setReferencedTestCase(referencedTestCase);
		executions.add(execution);
		updateExecutionStatus();

		//this means that getLastExecutedBy and getLastExecutedOn should be reset and propagated to the Iteration this object
		//is bound to.
		this.lastExecutedBy = null;
		this.lastExecutedOn = null;
		resetIterationDates();


	}

	private void resetIterationDates() {
		Iteration it = getIteration();
		if (it != null) {
			it.updateAutoDates(null);
		}
	}


	public void removeExecution(Execution execution){
		ListIterator<Execution> iterator = executions.listIterator();

		while (iterator.hasNext()){
			Execution exec = iterator.next();
			if (exec.getId().equals(execution.getId())){
				iterator.remove();
				break;
			}
		}

		updateExecutionStatus();
	}



	/**
	 * Factory method. Creates a copy of this object according to copy / paste rules.
	 *
	 * @return the copy, never <code>null</code>
	 */
	public IterationTestPlanItem createCopy() {
		IterationTestPlanItem copy = new IterationTestPlanItem();
		copy.setExecutionStatus(ExecutionStatus.READY);
		copy.setLabel(this.label);
		copy.setReferencedTestCase(this.referencedTestCase);
		return copy;
	}


	public Project getProject(){
		return iteration.getProject();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean isTestCaseDeleted(){
		return getReferencedTestCase() == null;
	}

}
