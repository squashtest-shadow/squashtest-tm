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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.testcase.TestCase;

@Entity
@PrimaryKeyJoinColumn(name = "CLN_ID")
public class Campaign extends CampaignLibraryNode implements AttachmentHolder {
	@Embedded
	private ScheduledTimePeriod scheduledPeriod = new ScheduledTimePeriod();
	@Embedded
	private final ActualTimePeriod actualPeriod = new ActualTimePeriod();

	@OneToMany(cascade = { CascadeType.MERGE })
	@OrderColumn(name = "ITERATION_ORDER")
	@JoinTable(name = "CAMPAIGN_ITERATION", joinColumns = @JoinColumn(name = "CAMPAIGN_ID"), inverseJoinColumns = @JoinColumn(name = "ITERATION_ID"))
	private final List<Iteration> iterations = new ArrayList<Iteration>();

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "TEST_PLAN_ORDER")
	@JoinColumn(name = "CAMPAIGN_ID")
	private final List<CampaignTestPlanItem> testPlan = new ArrayList<CampaignTestPlanItem>();

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	public Campaign() {
		super();
	}

	public Campaign(String name, String description) {
		super(name, description);
	}

	@Override
	public void accept(CampaignLibraryNodeVisitor visitor) {
		visitor.visit(this);

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

	public Date getActualStartDate() {
		return actualPeriod.getActualStartDate();
	}

	public void setActualStartDate(Date actualStartDate) {
		actualPeriod.setActualStartDate(actualStartDate);
	}

	public Date getActualEndDate() {
		return actualPeriod.getActualEndDate();
	}

	public List<CampaignTestPlanItem> getTestPlan() {
		return testPlan;
	}

	public void setActualEndDate(Date actualEndDate) {
		actualPeriod.setActualEndDate(actualEndDate);
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

	/**
	 * @deprecated use {@link #findTestPlanItem(TestCase)}
	 * @param testCaseId
	 * @return
	 */
	@Deprecated
	public CampaignTestPlanItem getTestPlanForTestPlanItemId(Long testCaseId) {
		for (CampaignTestPlanItem campTestPlan : this.getTestPlan()) {
			if (campTestPlan.getReferencedTestCase().getId().equals(testCaseId)) {
				return campTestPlan;
			}
		}
		return null;
	}

	/**
	 *
	 * @param testCase
	 * @return the test plan item which references the given test case, if any.
	 */
	public CampaignTestPlanItem findTestPlanItem(TestCase testCase) {
		for (CampaignTestPlanItem campTestPlan : this.getTestPlan()) {
			if (campTestPlan.getReferencedTestCase().equals(testCase)) {
				return campTestPlan;
			}
		}
		return null;
	}

	/**
	 *
	 * @param itemTestPlan
	 * @throws TestCaseAlreadyInTestPlanException
	 */
	public void addToTestPlan(@NotNull CampaignTestPlanItem itemTestPlan) throws TestCaseAlreadyInTestPlanException {
		if (testPlanContains(itemTestPlan.getReferencedTestCase())) {
			throw new TestCaseAlreadyInTestPlanException(itemTestPlan.getReferencedTestCase(), this);
		}

		this.getTestPlan().add(itemTestPlan);
		itemTestPlan.setCampaign(this);
	}

	public void removeTestPlanItem(@NotNull CampaignTestPlanItem itemTestPlan) {
		getTestPlan().remove(itemTestPlan);
	}

	public void removeIteration(@NotNull Iteration iteration) {
		getIterations().remove(iteration);
	}

	public List<Iteration> getIterations() {
		return iterations;
	}

	public void addIteration(@NotNull Iteration iteration) {
		getIterations().add(iteration);
		iteration.setCampaign(this);
	}

	private ScheduledTimePeriod getScheduledPeriod() {
		// Hibernate workaround : when STP fields are null, component is set to null
		if (scheduledPeriod == null) {
			scheduledPeriod = new ScheduledTimePeriod();
		}
		return scheduledPeriod;
	}

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	@Override
	public Campaign createPastableCopy() {
		Campaign copy = new Campaign();
		copy.setName(this.getName());
		copy.setDescription(this.getDescription());

		// as of 0.22.0 we do not copy actual start and end dates.
		if (this.getScheduledStartDate() != null) {
			copy.setScheduledStartDate((Date) this.getScheduledStartDate().clone());
		}
		if (this.getScheduledEndDate() != null) {
			copy.setScheduledEndDate((Date) this.getScheduledEndDate().clone());
		}


		for (Attachment tcAttach : this.getAttachmentList().getAllAttachments()) {
			Attachment atCopy = tcAttach.hardCopy();
			copy.getAttachmentList().addAttachment(atCopy);
		}

		for (CampaignTestPlanItem itemTestPlan : this.getTestPlan()) {
			copy.addToTestPlan(itemTestPlan.createCampaignlessCopy());
		}

		copy.notifyAssociatedWithProject(this.getProject());
		return copy;
	}

	public boolean isContentNameAvailable(String name) {
		for (Iteration content : getIterations()) {
			if (content.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	/* ******** dates autosetting code ***** */

	/**
	 * If the iteration have autodates set, they will be updated accordingly.
	 *
	 * @param newItemTestPlanDate
	 */
	public void updateActualStart(Date newIterationStartDate) {

		if (isActualStartAuto()) {
			// if we're lucky we can save a heavier computation
			if (getActualStartDate() == null) {
				setActualStartDate(newIterationStartDate);
			} else if ((newIterationStartDate != null) && (getActualStartDate().compareTo(newIterationStartDate) > 0)) {
				setActualStartDate(newIterationStartDate);
			}

			// well too bad, we have to recompute that.
			else {
				autoSetActualStartDate();
			}
		}
	}

	public void updateActualEnd(Date newIterationEndDate) {
		if (isActualEndAuto()) {
			// if we're lucky we can save a heavier computation
			if (getActualEndDate() == null) {
				setActualEndDate(newIterationEndDate);
			} else if ((newIterationEndDate != null) && (getActualEndDate().compareTo(newIterationEndDate) < 0)) {
				setActualEndDate(newIterationEndDate);
			}

			// well too bad, we have to recompute that.
			else {
				autoSetActualEndDate();
			}
		}

	}

	private void autoSetActualStartDate() {
		Date actualDate = getFirstIterationActualStartDate();

		setActualStartDate(actualDate);
	}

	private void autoSetActualEndDate() {
		Date actualDate = getLastIterationActualEndDate();

		setActualEndDate(actualDate);
	}

	private Date getFirstIterationActualStartDate() {
		if (getIterations().size() == 0) {
			return null;
		} else {
			Iteration firstIteration = Collections.min(getIterations(),
					CascadingAutoDateComparatorBuilder.buildIterationActualStartOrder());
			return firstIteration.getActualStartDate();
		}
	}

	private Date getLastIterationActualEndDate() {
		if (getIterations().size() == 0) {
			return null;
		} else {
			Iteration lastIteration = Collections.max(getIterations(),
					CascadingAutoDateComparatorBuilder.buildIterationActualEndOrder());
			return lastIteration.getActualEndDate();
		}
	}

	public boolean testPlanContains(@NotNull TestCase tc) {
		return (findTestPlanItem(tc) != null);
	}
	
	public boolean hasIterations(){
		return (iterations.size()>0);
	}
}
