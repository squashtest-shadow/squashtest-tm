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

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.security.annotation.InheritsAcls;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.EmptyTestPlanException;
import org.squashtest.csp.tm.domain.TestPlanItemNotExecutableException;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.audit.Auditable;

@Auditable
@Entity
@InheritsAcls(constrainedClass = Iteration.class, collectionName = "testSuites")
public class TestSuite {

	public TestSuite() {
		super();
	}

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@Basic(optional = false)
	@NotBlank
	private String name;

	@Lob
	private String description;

	@ManyToOne
	@JoinTable(name = "ITERATION_TEST_SUITE", joinColumns = @JoinColumn(name = "TEST_SUITE_ID", updatable = false, insertable = false), inverseJoinColumns = @JoinColumn(name = "ITERATION_ID", updatable = false, insertable = false))
	private Iteration iteration;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void rename(String newName) {
		if (!iteration.checkSuiteNameAvailable(newName)) {
			throw new DuplicateNameException("Cannot rename suite " + name + " : new name " + newName
					+ " already exists in iteration " + iteration.getName());
		}
		this.name = newName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * When one needs to create a suite in the scope of an iteration, it should use
	 * {@link Iteration#addTestSuite(TestSuite)}. This method is for internal use only.
	 * 
	 * @param iteration
	 */
	/* package */void setIteration(@NotNull Iteration iteration) {
		this.iteration = iteration;
	}

	public Iteration getIteration() {
		return iteration;
	}

	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	/**
	 * Warning : that property builds a new list every time. If you want to change the content of the list, use the
	 * other dedicated accessors ( {@link #addTestPlan(List))} or the other one)
	 * 
	 * @return
	 */
	public List<IterationTestPlanItem> getTestPlan() {
		// the test plan is not gotten through mapping hibernate because we need
		// the order that is only held by
		// iteration
		List<IterationTestPlanItem> testPlan = new LinkedList<IterationTestPlanItem>();
		for (IterationTestPlanItem item : iteration.getTestPlans()) {
			if (boundToThisSuite(item)) {
				testPlan.add(item);
			}
		}
		return testPlan;
	}

	private boolean boundToThisSuite(IterationTestPlanItem item) {
		TestSuite that = item.getTestSuite();

		return that != null && isSame(that);
	}

	/**
	 * Compares 2 suites, for internal use.
	 * 
	 * @param that
	 * @return
	 */
	private boolean isSame(TestSuite that) {
		if (this.id == null) {
			return this.equals(that);
		}
		// id not null -> persistent entity -> we cant use equals() because "that" might be a proxy so equals() would
		// return false
		return this.id.equals(that.getId());
	}

	public IterationTestPlanItem getFirstTestPlanItem() {
		for (IterationTestPlanItem item : iteration.getTestPlans()) {
			if (boundToThisSuite(item)) {
				return item;
			}
		}

		throw new EmptyTestPlanException(this);
	}

	/**
	 * Binds the test plan items to this test suite
	 * 
	 * @param items
	 */
	public void bindTestPlanItems(List<IterationTestPlanItem> items) {
		for (IterationTestPlanItem item : items) {
			item.setTestSuite(this);
		}
	}

	public void unBindTestPlan(List<IterationTestPlanItem> items) {
		for (IterationTestPlanItem item : items) {
			item.setTestSuite(null);
		}
	}

	/**
	 * Binds the test plan items to this test suite using their id to retrieve them from the iteration.
	 * 
	 * @param itemIds
	 */
	public void bindTestPlanItemsById(List<Long> itemIds) {
		for (Long itemId : itemIds) {
			for (IterationTestPlanItem item : iteration.getTestPlans()) {
				if (item.getId().equals(itemId)) {
					item.setTestSuite(this);
				}
			}
		}
	}

	/**
	 * Since the test plan of a TestSuite is merely a view on the backing iteration, we will reorder here the test plan
	 * accordingly. For instance if the newIndex is x in the TS test plan, Ix being the item at position x in the TS
	 * test plan, we will place the moved items at position y in Iteration test plan where y is the position of Ix in
	 * the Iteration test plan.
	 */
	public void reorderTestPlan(int newIndex, List<IterationTestPlanItem> movedItems) {

		IterationTestPlanItem anchorItem = getTestPlan().get(newIndex);
		Iteration iterationThis = getIteration();

		int anchorIndex = iterationThis.getIndexOf(anchorItem);
		iterationThis.moveTestPlans(anchorIndex, movedItems);

	}

	/**
	 * <p>
	 * returns an ordered copy of the test-suite test plan <br>
	 * -test plans items that are not linked to a test case are not copied<br>
	 * -the copy of a test plan item is done using {@linkplain IterationTestPlanItem#createCopy()}
	 * </p>
	 * 
	 * @return an ordered copy of the test-suite test plan
	 */
	public List<IterationTestPlanItem> createPastableCopyOfTestPlan() {
		List<IterationTestPlanItem> testPlanCopy = new LinkedList<IterationTestPlanItem>();
		List<IterationTestPlanItem> testPlanOriginal = this.getTestPlan();

		for (IterationTestPlanItem iterationTestPlanItem : testPlanOriginal) {
			if (!iterationTestPlanItem.isTestCaseDeleted()) {
				IterationTestPlanItem testPlanItemCopy = iterationTestPlanItem.createCopy();
				testPlanCopy.add(testPlanItemCopy);
			}
		}

		return testPlanCopy;

	}

	/**
	 * <p>
	 * returns a copy of a test Suite without it's test plan. <br>
	 * a copy of the test plan can be found at {@linkplain TestSuite#createPastableCopyOfTestPlan()}
	 * </p>
	 * 
	 * @return returns a copy of a test Suite
	 */
	public TestSuite createPastableCopy() {
		// the pastable copy of a test suite doesn't contain a test plan because
		// , if so, the test plan wouldn't be
		// reached with "getTestPlan()" because the test suite copy is not yet
		// linked to an iteration.
		TestSuite testSuiteCopy = new TestSuite();
		testSuiteCopy.setName(getName());
		testSuiteCopy.setDescription(getDescription());

		for (Attachment attach : this.getAttachmentList().getAllAttachments()) {
			Attachment copyAttach = attach.hardCopy();
			testSuiteCopy.getAttachmentList().addAttachment(copyAttach);
		}

		return testSuiteCopy;
	}

	public boolean isLastExecutableTestPlanItem(long itemId) {
		List<IterationTestPlanItem> testPlan = iteration.getTestPlans();
		for (int i = testPlan.size() - 1; i >= 0; i--) {
			IterationTestPlanItem item = testPlan.get(i);

			if (boundToThisSuite(item) && item.isExecutableThroughIteration()) {
				return itemId == item.getId();
			}
		}

		return false;
	}

	/**
	 * finds next item (that last execution has unexecuted step) or (has no execution and is not test case deleted)
	 * <em>can return item linked to test-case with no step</em>
	 * 
	 * @throws TestPlanItemNotExecutableException
	 *             if no item is found
	 * @throws IllegalArgumentException
	 *             if id does not correspond to an item of the test suite
	 * @param testPlanItemId
	 */
	public IterationTestPlanItem findNextExecutableTestPlanItem(long testPlanItemId) {
		List<IterationTestPlanItem> remaining = getRemainingPlanById(testPlanItemId);
		for (IterationTestPlanItem item : remaining) {
			if (item.isExecutableThroughTestSuite()) {
				return item;
			}
		}

		throw new TestPlanItemNotExecutableException("No more executable item in this suite's test plan");

	}

	/**
	 * @throws TestPlanItemNotExecutableException
	 * @return
	 */
	public IterationTestPlanItem findFirstExecutableTestPlanItem() {
		List<IterationTestPlanItem> testPlan = this.getTestPlan();
		if (!testPlan.isEmpty()) {
			if (testPlan.get(0).isExecutableThroughTestSuite()) {
				return testPlan.get(0);
			} else {
				return findNextExecutableTestPlanItem(testPlan.get(0).getId());
			}
		}
		throw new TestPlanItemNotExecutableException("No executable item in this suite's test plan");
	}

	private List<IterationTestPlanItem> getRemainingPlanById(long testPlanItemId) {
		List<IterationTestPlanItem> testPlan = this.getTestPlan();

		for (int i = 0; i < testPlan.size(); i++) {
			if (testPlanItemId == testPlan.get(i).getId()) {
				return testPlan.subList(i + 1, testPlan.size());
			}
		}

		throw new IllegalArgumentException("Item[" + testPlanItemId + "] does not belong to test plan of TestSuite["
				+ id + ']');
	}

}
