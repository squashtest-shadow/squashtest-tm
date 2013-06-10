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
package org.squashtest.tm.domain.campaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.library.HasExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.NotAutomatedException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.security.annotation.InheritsAcls;

@Entity
@Auditable
@InheritsAcls(constrainedClass = Iteration.class, collectionName = "testPlans")
public class IterationTestPlanItem implements HasExecutionStatus , Identified{

	private static final Set<ExecutionStatus> LEGAL_EXEC_STATUS;

	static {
		Set<ExecutionStatus> set = new HashSet<ExecutionStatus>(Arrays.asList(ExecutionStatus.values()));
		LEGAL_EXEC_STATUS = Collections.unmodifiableSet(set);
	}

	@Id
	@GeneratedValue
	@Column(name = "ITEM_TEST_PLAN_ID")
	private Long id;

	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.READY;

	private String label = "";

	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private User user;

	@Column(insertable = false)
	private String lastExecutedBy;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastExecutedOn;

	@ManyToOne
	@JoinColumn(name = "TCLN_ID", referencedColumnName = "TCLN_ID")
	private TestCase referencedTestCase;

	@ManyToOne
	@JoinColumn(name = "DATASET_ID", referencedColumnName = "DATASET_ID")
	private Dataset referencedDataset;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "EXECUTION_ORDER")
	@JoinTable(name = "ITEM_TEST_PLAN_EXECUTION", joinColumns = @JoinColumn(name = "ITEM_TEST_PLAN_ID"), inverseJoinColumns = @JoinColumn(name = "EXECUTION_ID"))
	private final List<Execution> executions = new ArrayList<Execution>();

	@ManyToOne
	@JoinTable(name = "ITEM_TEST_PLAN_LIST", joinColumns = @JoinColumn(name = "ITEM_TEST_PLAN_ID", insertable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "ITERATION_ID", insertable = false, updatable = false))
	private Iteration iteration;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy="testPlan")
	private List<TestSuite> testSuites = new ArrayList<TestSuite>();

	public IterationTestPlanItem() {
		super();
	}

	public Iteration getIteration() {
		return iteration;
	}

	public IterationTestPlanItem(TestCase testCase) {
		referencedTestCase = testCase;
		referencedDataset = null;
		label = testCase.getName();
	}

	public IterationTestPlanItem(TestCase testCase, Dataset dataset) {
		referencedTestCase = testCase;
		referencedDataset = dataset;
		label = testCase.getName();
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

	/**
	 * the IterationTestPlanItem will fetch the ExecutionStatus of the last "live" Execution in his execution list
	 * 
	 */
	public void updateExecutionStatus() {
		if (executions.isEmpty()) {
			executionStatus = ExecutionStatus.READY;
			return;
		} else {
			Execution execution = getLatestExecution();
			executionStatus = execution.getExecutionStatus();
		}
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

		if (getIteration() != null) {
			getIteration().updateAutoDates(lastExecutedOn);
		}
	}

	public void emptyExecutions() {
		this.executions.clear();
	}

	public void addExecution(@NotNull Execution execution) {
		executions.add(execution);
		execution.notifyAddedTo(this);
		updateExecutionStatus();

		// this means that getLastExecutedBy and getLastExecutedOn should be
		// reset and propagated to the Iteration this
		// object
		// is bound to.
		this.lastExecutedBy = null;
		this.lastExecutedOn = null;
		resetIterationDates();

	}

	/**
	 * Creates an execution of this item and returns it.
	 * 
	 * <h3>WARNING</h3>
	 * <p>Will not check cyclic calls between the referenced test cases anymore (eg A calls B calls C calls A). You have been warned</p>
	 * 
	 * @return the new execution
	 */
	public Execution createExecution()	throws TestPlanItemNotExecutableException {
		
		checkExecutable();
		Execution newExecution = null;
		
		if(this.referencedDataset != null){
			newExecution = new Execution(referencedTestCase, referencedDataset);
		} else {
			newExecution = new Execution(referencedTestCase);
		}
		

		return newExecution;
	}

	public Execution createAutomatedExecution()
			throws TestPlanItemNotExecutableException {

		if (!isAutomated()) {
			throw new NotAutomatedException();
		}

		Execution execution = createExecution();

		AutomatedExecutionExtender extender = new AutomatedExecutionExtender();
		extender.setAutomatedTest(referencedTestCase.getAutomatedTest());
		extender.setExecution(execution);
		execution.setAutomatedExecutionExtender(extender);

		return execution;

	}

	private void checkExecutable() throws TestPlanItemNotExecutableException {
		if (!isExecutableThroughIteration()) {
			throw new TestPlanItemNotExecutableException("Test case referenced by this item was deleted");
		}

	}

	public boolean isAutomated() {
		if (referencedTestCase == null) {
			return false;
		}
		return referencedTestCase.isAutomated();
	}

	private void resetIterationDates() {
		Iteration it = getIteration();
		if (it != null) {
			it.updateAutoDates(null);
		}
	}

	public void removeExecution(Execution execution) {
		boolean wasLastExecution = false;
		if (this.getLatestExecution().equals(execution)) {
			wasLastExecution = true;
		}
		ListIterator<Execution> iterator = executions.listIterator();

		while (iterator.hasNext()) {
			Execution exec = iterator.next();
			if (exec.getId().equals(execution.getId())) {
				iterator.remove();
				break;
			}
		}
		if (wasLastExecution) {
			updateExecutionStatus();
			if (this.getLatestExecution() != null) {
				this.lastExecutedOn = this.getLatestExecution().getLastExecutedOn();
				this.lastExecutedBy = this.getLatestExecution().getLastExecutedBy();
			} else {
				this.lastExecutedOn = null;
				this.lastExecutedBy = null;
			}
			Iteration iter = this.getIteration();

			if (iter != null) {
				iter.updateAutoDatesAfterExecutionDetach(this, execution);
			}
		}

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
		copy.setUser(this.user);

		return copy;
	}

	public Project getProject() {
		return iteration.getProject();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isExecutableThroughIteration() {
		return !isTestCaseDeleted();
	}

	/**
	 * @return true if (the item last execution with unexecuted step) or (item has no execution and is linked to a
	 *         testCase).
	 */
	public boolean isExecutableThroughTestSuite() {
		if (executions.isEmpty()) {
			return !this.isTestCaseDeleted();
		} else {
			return isLatestExecutionStillRunning();
		}
	}

	private boolean isLatestExecutionStillRunning() {
		return getLatestExecution().hasUnexecutedSteps();
	}

	/**
	 * One should use {@link #isExecutableThroughIteration()} in favor of this method.
	 * 
	 * @return
	 */
	public boolean isTestCaseDeleted() {
		return getReferencedTestCase() == null;
	}
	
	/**
	 * Checks id equality in case the comparison fails because in some cases,
	 * hibernate proxies make the comparison fail.
	 */
	private boolean isSameIteration(Iteration thisIteration, Iteration thatIteration){
		
		boolean result = false;

		if(thisIteration.equals(thatIteration)){
			result = true;
		} else {
			result = false;
			if(thisIteration.getId() != null && thatIteration.getId() != null){
				result = thisIteration.getId().equals(thatIteration.getId());
			} 
		}
		
		return result;
	}
	
	public void addTestSuite(@NotNull TestSuite suite) {
		if (!isSameIteration(this.iteration, suite.getIteration())) {
			throw new IllegalArgumentException("Item[" + id + "] dont belong to Iteration["
					+ suite.getIteration().getId() + "], it cannot be bound to TestSuite['" + suite.getName() + "']");
		}
		this.testSuites.add(suite);
		suite.bindTestPlanItem(this);
	}

	public void removeTestSuite(TestSuite suite) {
		long suiteId = suite.getId();
		List<TestSuite> toRemove = new ArrayList<TestSuite>();
		for(TestSuite testSuite : this.testSuites){
			if(testSuite.getId() == suiteId){
				toRemove.add(testSuite);
				suite.unBindTestPlan(this);
			}
		}
		this.testSuites.removeAll(toRemove);
	}

	public List<TestSuite> getTestSuites() {
		return this.testSuites;
	}

	public String getTestSuiteNames() {
		
		StringBuilder builder = new StringBuilder();
		
		for(TestSuite suite : testSuites){
			builder.append(suite.getName() +", ");
		}
		String nameList = builder.toString();
		if(nameList.length() > 0){
			nameList = nameList.trim().substring(0, nameList.lastIndexOf(","));
		}

		return nameList;
	}
	
	public void setTestSuites(List<TestSuite> testSuites) {
		this.testSuites = testSuites;
	}

	
	/* package */void setIteration(Iteration iteration) {
		this.iteration = iteration;

	}

	/**
	 * 
	 * @return the last {@linkplain Execution} or null if there is none
	 */
	public Execution getLatestExecution() {
		if (!executions.isEmpty()) {
			return executions.get(executions.size() - 1);
		}
		return null;
	}
	
	public TestCaseExecutionMode getExecutionMode() {
		Execution latest = getLatestExecution();
		
		return latest == null ? TestCaseExecutionMode.UNDEFINED : latest.getExecutionMode();
	}

	public Dataset getReferencedDataset() {
		return referencedDataset;
	}

	public void setReferencedDataset(Dataset referencedDataset) {
		this.referencedDataset = referencedDataset;
	}
}
