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
package org.squashtest.tm.domain.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
import javax.validation.constraints.Size;

import org.apache.commons.lang.NullArgumentException;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.library.Copiable;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.softdelete.SoftDeletable;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.service.security.annotation.AclConstrainedObject;

@Auditable
@Entity
@SoftDeletable
public class Iteration implements AttachmentHolder, NodeContainer<TestSuite>, TreeNode, Copiable, Identified, BoundEntity{

	private static final String ITERATION_ID = "ITERATION_ID";

	@Id
	@GeneratedValue
	@Column(name = ITERATION_ID)
	private Long id;

	@Lob
	private String description;

	@NotBlank
	@Size(min = 0, max = 255)
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
	@Override
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

	/**
	 * <p>
	 * copy of iteration <u>doesn't contain test-suites</u> !!<br>
	 * </p>
	 * 
	 * @return
	 */
	@Override
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

	@Override
	public Project getProject() {
		if(campaign!=null){
		return campaign.getProject();
		}else{
			return null;
		}
	}

	@AclConstrainedObject
	public CampaignLibrary getCampaignLibrary() {
		return getProject().getCampaignLibrary();
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
		Date actualDate = null;
		if (actualEndDateUpdateAuthorization()) {
			actualDate = getLastExecutedTestPlanDate();
		}
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

	/**
	 * this method is used in case of copy paste of an iteration with test suites.<br>
	 * 
	 * @return A map of test suite and indexes<br>
	 *         One entry-set contains
	 *         <ul>
	 *         <li>a copied test suite (without it's test plan)</li>
	 *         <li>and the indexes of the copied test plan that are to be linked with it
	 *         <em>(taking into account test_plan_items that are test_case deleted)</em></li>
	 */
	public Map<TestSuite, List<Integer>> createTestSuitesPastableCopy() {
		Map<TestSuite, List<Integer>> resultMap = new HashMap<TestSuite, List<Integer>>();
		List<IterationTestPlanItem> testPlanWithoutDeletedTestCases = getTestPlanWithoutDeletedTestCases();
		
		for (TestSuite testSuite : getTestSuites()) {
			List<IterationTestPlanItem> testSuiteTestPlan = testSuite.getTestPlan();
			TestSuite testSuiteCopy = testSuite.createCopy();
			List<Integer> testPlanIndex = new ArrayList<Integer>();
			
			for (IterationTestPlanItem iterationTestPlanItem : testSuiteTestPlan) {
				int testPlanItemIndex = testPlanWithoutDeletedTestCases.indexOf(iterationTestPlanItem);
				testPlanIndex.add(testPlanItemIndex);
			}
			
			resultMap.put(testSuiteCopy, testPlanIndex);
		}
		
		return resultMap;
	}

	private List<IterationTestPlanItem> getTestPlanWithoutDeletedTestCases() {
		List<IterationTestPlanItem> testPlanResult = getTestPlans();
		Iterator<IterationTestPlanItem> iterator = testPlanResult.iterator();
		while (iterator.hasNext()) {
			IterationTestPlanItem itpi = iterator.next();
			if (itpi.isTestCaseDeleted()) {
				testPlanResult.remove(itpi);
			}
		}
		return testPlanResult;
	}

	/**
	 * will update acual end and start dates if are auto and if they were driven by the execution last-executed on
	 * 
	 * @param execution
	 */
	public void updateAutoDatesAfterExecutionDetach(IterationTestPlanItem iterationTestPlanItem, Execution execution) {

		updateAutoEndDateAfterExecutionDetach(iterationTestPlanItem, execution);
		updateStartAutoDateAfterExecutionDetach(execution);

	}

	private void updateStartAutoDateAfterExecutionDetach(Execution execution) {
		if (this.isActualStartAuto()) {
			autoSetActualStartDate();
		}

	}

	private void updateAutoEndDateAfterExecutionDetach(IterationTestPlanItem iterationTestPlanItem, Execution execution) {
		if (this.isActualEndAuto()) {
			if (!iterationTestPlanItem.getExecutionStatus().isTerminatedStatus()) {
				this.setActualEndDate(null);
			} else {
				autoSetActualEndDate();
			}
		}

	}
	
	// ***************** (detached) custom field section *************
	
	@Override
	public Long getBoundEntityId() {
		return getId();
	}
	
	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.ITERATION;
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void addContent(@NotNull TestSuite testSuite) throws DuplicateNameException, NullArgumentException {
		this.addTestSuite((TestSuite) testSuite);
		
	}

	@Override
	public boolean isContentNameAvailable(String name) {
		return checkSuiteNameAvailable(name);
	}

	@Override
	public Set getContent() {
		return getTestSuites();
	}
	
	@Override
	public boolean hasContent(){
		return !getContent().isEmpty();
	}

	@Override
	public void removeContent(TestSuite contentToRemove) throws NullArgumentException {
		removeTestSuite(contentToRemove);
		
	}

	@Override
	public List<String> getContentNames() {
		List<String> testSuitesNames = new ArrayList<String>(testSuites.size());
		for(TestSuite suite : testSuites){
			testSuitesNames.add(suite.getName());
		}
		return testSuitesNames;
	}

	

}
