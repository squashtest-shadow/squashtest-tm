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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.security.annotation.AclConstrainedObject;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.UnknownEntityException;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.softdelete.SoftDeletable;
import org.squashtest.csp.tm.domain.testcase.TestCase;

@Auditable
@Entity
@SoftDeletable
public class Iteration implements AttachmentHolder {

	private static final String ITERATION_ID = "ITERATION_ID";

	@Id
	@GeneratedValue
	@Column(name = ITERATION_ID)
	private Long id;

	@Lob
	private String description;

	@Basic(optional = false)
	private String name;

	@Embedded
	private ScheduledTimePeriod scheduledPeriod = new ScheduledTimePeriod();

	@Embedded
	private final ActualTimePeriod actualPeriod = new ActualTimePeriod();

	/*
	 * read http://docs.redhat.com/docs/en-US/JBoss_Enterprise_Web_Platform/5/html
	 * /Hibernate_Annotations_Reference_Guide /entity-mapping-association-collection-onetomany.html
	 *
	 * "To map a bidirectional one to many, with the one-to-many side as the owning side, you have to remove the
	 * mappedBy element and set the many to one @JoinColumn as insertable and updatable to false. This solution is
	 * obviously not optimized and will produce some additional UPDATE statements."
	 *
	 * The reason for this is because Hibernate doesn't support the correct mapping (using mappingBy and @OrderColumns).
	 * The solution used here is only a workaround.
	 *
	 * See bug HHH-5390 for a concise discussion about this.
	 */

	@ManyToOne
	@JoinTable(name = "CAMPAIGN_ITERATION", joinColumns = @JoinColumn(name = ITERATION_ID, updatable = false, insertable = false), inverseJoinColumns = @JoinColumn(name = "CAMPAIGN_ID", updatable = false, insertable = false))
	private Campaign campaign;

	/*
	 * FIXME TEST_PLAN might be a little more appropriate. Don't forget to fix the hql/criteria queries as well
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "ITEM_TEST_PLAN_ORDER")
	@JoinTable(name = "ITEM_TEST_PLAN_LIST", joinColumns = @JoinColumn(name = ITERATION_ID), inverseJoinColumns = @JoinColumn(name = "ITEM_TEST_PLAN_ID"))
	private final List<IterationTestPlanItem> testPlans = new ArrayList<IterationTestPlanItem>();

	/* *********************** attachment attributes ************************ */

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	/* *********************** Test suites ********************************** */

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "ITERATION_TEST_SUITE", joinColumns = @JoinColumn(name = ITERATION_ID), inverseJoinColumns = @JoinColumn(name = "TEST_SUITE_ID"))
	private Set<TestSuite> testSuites = new HashSet<TestSuite>();

	/**
	 * flattened list of the executions
	 */
	public List<Execution> getExecutions() {
		List<Execution> listExec = new ArrayList<Execution>();
		for (IterationTestPlanItem testplan : testPlans) {
			listExec.addAll(testplan.getExecutions());
		}

		return listExec;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NotBlank
	public String getName() {
		return this.name;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setScheduledStartDate(Date startDate) {
		getScheduledPeriod().setScheduledStartDate(startDate);
	}

	public Date getScheduledStartDate() {
		return getScheduledPeriod().getScheduledStartDate();
	}

	public void setScheduledEndDate(Date endDate) {
		getScheduledPeriod().setScheduledEndDate(endDate);
	}

	public Date getScheduledEndDate() {
		return getScheduledPeriod().getScheduledEndDate();
	}

	public void setActualStartDate(Date startDate) {
		actualPeriod.setActualStartDate(startDate);
		if (getCampaign() != null) {
			getCampaign().updateActualStart(startDate);
		}
	}

	public Date getActualStartDate() {
		return actualPeriod.getActualStartDate();
	}

	public void setActualEndDate(Date endDate) {
		actualPeriod.setActualEndDate(endDate);
		if (getCampaign() != null) {
			getCampaign().updateActualEnd(endDate);
		}
	}

	public Date getActualEndDate() {
		return actualPeriod.getActualEndDate();
	}

	public boolean isActualStartAuto() {
		return actualPeriod.isActualStartAuto();
	}

	public boolean isActualEndAuto() {
		return actualPeriod.isActualEndAuto();
	}

	public void setActualStartAuto(boolean actualStartAuto) {
		actualPeriod.setActualStartAuto(actualStartAuto);

		if (actualPeriod.isActualStartAuto()) {
			autoSetActualStartDate();
		}
	}

	public void setActualEndAuto(boolean actualEndAuto) {
		actualPeriod.setActualEndAuto(actualEndAuto);

		if (actualPeriod.isActualEndAuto()) {
			autoSetActualEndDate();
		}

	}

	public Long getId() {
		return id;
	}

	private ScheduledTimePeriod getScheduledPeriod() {
		// Hibernate workaround : when STP fields are null, component is set to
		// null
		if (scheduledPeriod == null) {
			scheduledPeriod = new ScheduledTimePeriod();
		}
		return scheduledPeriod;
	}

	private TestSuite getTestSuite(Long testSuiteId) {
		for (TestSuite testSuite : testSuites) {
			if (testSuite.getId().equals(testSuiteId)) {
				return testSuite;
			}
		}
		throw new UnknownEntityException(testSuiteId, TestSuite.class);
	}

	public IterationTestPlanItem getTestPlan(Long testPlanId) {
		for (IterationTestPlanItem iterTestPlan : testPlans) {
			if (iterTestPlan.getId().equals(testPlanId)) {
				return iterTestPlan;
			}
		}
		throw new UnknownEntityException(testPlanId, IterationTestPlanItem.class);
	}

	public Iteration createCopy() {
		Iteration clone = new Iteration();
		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		copyPlanning(clone);

		for (IterationTestPlanItem itemTestPlan : testPlans) {
			clone.addTestPlan(itemTestPlan.createCopy());
		}
		for (Attachment attach : this.getAttachmentList().getAllAttachments()) {
			Attachment copyAttach = attach.hardCopy();
			clone.getAttachmentList().addAttachment(copyAttach);
		}

		return clone;
	}

	/**
	 * copy planning info: <br>
	 * if actual end/start is auto => don't copy the actual date.
	 *
	 * @param clone
	 */
	private void copyPlanning(Iteration clone) {
		clone.setActualEndAuto(this.isActualEndAuto());
		clone.setActualStartAuto(this.isActualStartAuto());

		if (this.getScheduledStartDate() != null) {
			clone.setScheduledStartDate((Date) this.getScheduledStartDate().clone());
		}
		if (this.getScheduledEndDate() != null) {
			clone.setScheduledEndDate((Date) this.getScheduledEndDate().clone());
		}
		if (this.getActualStartDate() != null && !this.isActualStartAuto()) {
			clone.setActualStartDate((Date) this.getActualStartDate().clone());
		}

		if (this.getActualEndDate() != null && !this.isActualEndAuto()) {
			clone.setActualEndDate((Date) this.getActualEndDate().clone());
		}
	}

	/*
	 * **************************************** TEST PLAN ****************************************************
	 */

	public List<IterationTestPlanItem> getTestPlans() {
		return testPlans;
	}

	public List<TestCase> getPlannedTestCase() {
		List<TestCase> list = new LinkedList<TestCase>();
		for (IterationTestPlanItem iterTestPlan : testPlans) {
			list.add(iterTestPlan.getReferencedTestCase());
		}
		return list;
	}

	public void removeTestSuite(@NotNull TestSuite testSuite) {
		TestSuite localTestSuite = getTestSuite(testSuite.getId());

		if (localTestSuite != null) {
			testSuites.remove(testSuite);
		}

	}

	public void removeTestPlan(@NotNull IterationTestPlanItem testPlan) {
		IterationTestPlanItem localTestPlan = getTestPlan(testPlan.getId());

		if (localTestPlan != null) {
			testPlans.remove(testPlan);
		}

	}

	// TODO have a addToTestPlan(TestCase) method instead / also
	public void addTestPlan(@NotNull IterationTestPlanItem testPlan) {
		// TODO undocumented behaviour which silently breaks what the method is
		// supposed to do. gotta come up with something better
		if (testPlan.getReferencedTestCase() == null) {
			return;
		}
		testPlans.add(testPlan);
		testPlan.setIteration(this);
	}

	/***
	 * Method which returns the position of a test case in the current iteration
	 *
	 * @param testCaseId
	 *            the id of the test case we're looking for
	 * @return the position of the test case (int)
	 * @throws UnknownEntityException
	 *             if not found.
	 */
	public int findTestCaseIndexInTestPlan(long testCaseId) {
		ListIterator<IterationTestPlanItem> iterator = testPlans.listIterator();
		while (iterator.hasNext()) {
			IterationTestPlanItem itemTestPlan = iterator.next();

			if ((!itemTestPlan.isTestCaseDeleted())
					&& (itemTestPlan.getReferencedTestCase().getId().equals(testCaseId))) {
				return iterator.previousIndex();
			}
		}

		throw new UnknownEntityException(testCaseId, TestCase.class);

	}

	/***
	 * Method which returns the position of an item test plan in the current iteration
	 *
	 * @param testPlanId
	 *            the id of the test plan we're looking for
	 * @return the position of the test plan (int)
	 * @throws UnknownEntityException
	 *             if not found.
	 */
	public int findItemIndexInTestPlan(long testPlanId) {

		ListIterator<IterationTestPlanItem> iterator = testPlans.listIterator();
		while (iterator.hasNext()) {
			IterationTestPlanItem itemTestPlan = iterator.next();

			if (itemTestPlan.getId().equals(testPlanId)) {

				return iterator.previousIndex();
			}
		}

		throw new UnknownEntityException(testPlanId, IterationTestPlanItem.class);

	}

	/***
	 * Method which sets a test case at a new position
	 *
	 * @param currentPosition
	 *            the current position
	 * @param newPosition
	 *            the new position
	 */
	@Deprecated
	public void moveTestPlan(int currentPosition, int newPosition) {
		if (currentPosition == newPosition) {
			return;
		}

		IterationTestPlanItem testCaseToMove = testPlans.get(currentPosition);
		testPlans.remove(currentPosition);
		testPlans.add(newPosition, testCaseToMove);
	}

	public void moveTestPlans(int newIndex, List<IterationTestPlanItem> movedItems) {
		if (!testPlans.isEmpty()) {
			testPlans.removeAll(movedItems);
			testPlans.addAll(newIndex, movedItems);
		}
	}

	/* returns the index of that item if found, -1 if not found */
	public int getIndexOf(IterationTestPlanItem item) {

		int i = 0;
		ListIterator<IterationTestPlanItem> iterator = testPlans.listIterator();

		while (iterator.hasNext()) {
			if (item.equals(iterator.next())) {
				return i;
			}
			i++;
		}

		return -1;
	}

	public boolean isTestCasePlanned(Long testCaseId) {
		return (getTestPlanForTestCaseId(testCaseId) != null);
	}

	public boolean isTestCasePlanned(TestCase testCase) {
		return isTestCasePlanned(testCase.getId());
	}

	// get a test plan if the provided test case is part of it
	// returns null otherwise
	public IterationTestPlanItem getTestPlanForTestCaseId(Long testCaseId) {
		for (IterationTestPlanItem iterTestPlan : testPlans) {
			if ((!iterTestPlan.isTestCaseDeleted())
					&& (iterTestPlan.getReferencedTestCase().getId().equals(testCaseId))) {
				return iterTestPlan;
			}
		}
		return null;
	}

	/*
	 * ********************************* TEST SUITE *********************************************
	 */

	public Set<TestSuite> getTestSuites() {
		return testSuites;
	}

	public void addTestSuite(TestSuite suite) {
		if (!checkSuiteNameAvailable(suite.getName())) {
			throw new DuplicateNameException("cannot add suite to iteration " + getName() + " : suite named "
					+ suite.getName() + " already exists");
		}
		testSuites.add(suite);
		suite.setIteration(this);
	}

	public boolean checkSuiteNameAvailable(String name) {
		for (TestSuite suite : testSuites) {
			if (suite.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	public boolean hasTestSuites() {
		return (testSuites.size() > 0);
	}

	/*
	 * ********************************************** Attachable implementation
	 * ******************************************
	 */

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	@AclConstrainedObject
	public Project getProject() {
		return campaign.getProject();
	}

	/*
	 * *********************************************** dates autosetting code
	 * ********************************************
	 */

	/**
	 * If the iteration have autodates set, they will be updated accordingly.
	 *
	 * @param newItemTestPlanDate
	 */
	public void updateAutoDates(Date newItemTestPlanDate) {

		if (isActualStartAuto()) {
			updateAutoDatesAcutalStart(newItemTestPlanDate);
		}
		// check also if the end end can be updated
		if (isActualEndAuto()) {
			updateAutoDatesActualEnd(newItemTestPlanDate);
		}

	}

	private void updateAutoDatesActualEnd(Date newItemTestPlanDate) {
		if (actualEndDateUpdateAuthorization()) {
			// if we're lucky we can save a heavier computation
			if (getActualEndDate() == null) {
				setActualEndDate(newItemTestPlanDate);
			} else if ((newItemTestPlanDate != null) && (getActualEndDate().compareTo(newItemTestPlanDate) < 0)) {
				setActualEndDate(newItemTestPlanDate);
			}

			// well too bad, we have to recompute that.
			else {
				autoSetActualEndDateNoCheck();
			}
		} else {
			setActualEndDate(null);
		}
	}

	private void updateAutoDatesAcutalStart(Date newItemTestPlanDate) {
		// if we're lucky we can save a heavier computation
		if (getActualStartDate() == null) {
			setActualStartDate(newItemTestPlanDate);
		} else if ((newItemTestPlanDate != null) && (getActualStartDate().compareTo(newItemTestPlanDate) > 0)) {
			setActualStartDate(newItemTestPlanDate);
		}

		// well too bad, we have to recompute that.
		else {
			autoSetActualStartDate();
		}
	}

	private void autoSetActualStartDate() {
		Date actualDate = getFirstExecutedTestPlanDate();

		setActualStartDate(actualDate);
	}

	/***
	 * Same method as autoSetActualEndDate but without actualEndDateUpdateAuthorization call To avoid checking
	 * authorization twice
	 */
	private void autoSetActualEndDateNoCheck() {
		Date actualDate = getLastExecutedTestPlanDate();
		setActualEndDate(actualDate);
	}

	private void autoSetActualEndDate() {
		// Check if end date can be set
		Date actualDate = (actualEndDateUpdateAuthorization()) ? getLastExecutedTestPlanDate() : null;

		setActualEndDate(actualDate);
	}

	/***
	 * This methods browses testPlans and checks if at least one testPlanItem has RUNNING or READY for execution status.
	 * If this is the case, the actualEndDate should not be set
	 *
	 * @return false if the date should not be set
	 */
	private boolean actualEndDateUpdateAuthorization() {
		boolean toReturn = true;
		for (IterationTestPlanItem testPlanItem : testPlans) {
			if (!testPlanItem.getExecutionStatus().isTerminatedStatus()) {
				toReturn = false;
			}
		}
		return toReturn;
	}

	private Date getFirstExecutedTestPlanDate() {
		if (getTestPlans().size() == 0) {
			return null;
		} else {
			IterationTestPlanItem firstTestPlan = Collections.min(getTestPlans(),
					CascadingAutoDateComparatorBuilder.buildTestPlanFirstDateSorter());
			return firstTestPlan.getLastExecutedOn();
		}
	}

	private Date getLastExecutedTestPlanDate() {
		if (getTestPlans().size() == 0) {
			return null;
		} else {
			IterationTestPlanItem lastTestPlan = Collections.max(getTestPlans(),
					CascadingAutoDateComparatorBuilder.buildTestPlanLastDateSorter());
			return lastTestPlan.getLastExecutedOn();
		}
	}

}
