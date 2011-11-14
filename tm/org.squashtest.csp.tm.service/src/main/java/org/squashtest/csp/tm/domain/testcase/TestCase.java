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
package org.squashtest.csp.tm.domain.testcase;

import static org.squashtest.csp.tm.domain.testcase.TestCaseImportance.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.UnknownEntityException;
import org.squashtest.csp.tm.domain.attachment.Attachable;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.requirement.Requirement;

/**
 * @author Gregory Fouquet
 *
 */
@Entity
@PrimaryKeyJoinColumn(name = "TCLN_ID")
public class TestCase extends TestCaseLibraryNode implements Attachable {
	private static final String CLASS_NAME = "org.squashtest.csp.tm.domain.testcase.TestCase";
	private static final String SIMPLE_CLASS_NAME = "TestCase";

	@Column(updatable = false)
	private final int version = 1;

	@Enumerated(EnumType.STRING) @Basic(optional=false)
	private TestCaseExecutionMode executionMode = TestCaseExecutionMode.MANUAL;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "STEP_ORDER")
	@JoinTable(name = "TEST_CASE_STEPS", joinColumns = @JoinColumn(name = "TEST_CASE_ID"), inverseJoinColumns = @JoinColumn(name = "STEP_ID"))
	private final List<TestStep> steps = new ArrayList<TestStep>();

	/**
	 * Collection of {@link Requirement}s which are verified by this {@link TestCase}
	 */
	@ManyToMany
	@JoinTable(name = "TEST_CASE_REQUIREMENT_LINK", joinColumns = @JoinColumn(name = "TEST_CASE_ID"), inverseJoinColumns = @JoinColumn(name = "REQUIREMENT_ID"))
	private final Set<Requirement> verifiedRequirements = new HashSet<Requirement>();

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentCollection = new AttachmentList();
	
	@Enumerated(EnumType.STRING) @Basic(optional=false)
	private TestCaseImportance importance = LOW;
	
	/**
	 * Should the importance be automatically computed. 
	 */
	private boolean importanceAuto = false;
	
	public TestCase() {
		super();
	}

	public int getVersion() {
		return version;
	}

	public void setExecutionMode(TestCaseExecutionMode exectionMode) {
		this.executionMode = exectionMode;
	}

	public TestCaseExecutionMode getExecutionMode() {
		return executionMode;
	}

	public List<TestStep> getSteps() {
		return steps;
	}

	public void addStep(@NotNull TestStep step) {
		getSteps().add(step);
	}

	public void addStep(int index, @NotNull TestStep step) {
		getSteps().add(index, step);
	}

	public void moveStep(int stepIndex, int newIndex) {
		if (stepIndex == newIndex) {
			return;
		}
		TestStep step = getSteps().get(stepIndex);
		getSteps().remove(stepIndex);
		getSteps().add(newIndex, step);
	}
	
	/**
	 * Will move a list of steps to a new position.
	 * 
	 * @param newIndex the position we want the first element of movedSteps to be once the operation is complete
	 * @param movedSteps the list of steps to move, sorted by rank among each others.
	 */
	public void moveSteps(int newIndex, List<TestStep> movedSteps){
		if (! steps.isEmpty()){
			steps.removeAll(movedSteps);
			steps.addAll(newIndex, movedSteps);				
		}
	}

	public Set<Requirement> getVerifiedRequirements() {
		return verifiedRequirements;
	}

	@Override
	public void accept(TestCaseLibraryNodeVisitor visitor) {
		visitor.visit(this);

	}

	/**
	 * Adds a {@link Requirement} verified by this {@link TestCase}
	 *
	 * @param requirement
	 *            requirement to add, should not be null.
	 */
	public void addVerifiedRequirement(@NotNull Requirement requirement) {
		getVerifiedRequirements().add(requirement);
	}

	public void removeVerifiedRequirement(@NotNull Requirement requirement) {
		getVerifiedRequirements().remove(requirement);
	}

	@Override
	public Long getAttachmentCollectionId() {
		return attachmentCollection.getId();
	}

	@Override
	public AttachmentList getAttachmentCollection() {
		return attachmentCollection;
	}

	@Override
	public boolean hasAttachments() {
		return getAttachmentCollection().hasAttachments();
	}

	@Override
	public int getNbAttachments() {

		return getAttachmentCollection().size();
	}

	public Set<Attachment> getAllAttachments() {
		return attachmentCollection.getAllAttachments();
	}

	@Override
	public TestCase createCopy() {
		TestCase copy = new TestCase();
		copy.setSimplePropertiesUsing(this);
		copy.addCopiesOfSteps(this);
		copy.addCopiesOfAttachments(this);
		copy.verifiesRequirementsVerifiedBy(this);
		copy.notifyAssociatedWithProject(this.getProject());

		return copy;
	}

	private void addCopiesOfAttachments(TestCase source) {
		for (Attachment tcAttach : source.getAttachmentCollection().getAllAttachments()) {
			Attachment atCopy = tcAttach.hardCopy();
			this.getAttachmentCollection().addAttachment(atCopy);
		}
	}

	private void addCopiesOfSteps(TestCase source) {
		for (TestStep testStep : source.getSteps()) {
			this.addStep(testStep.createCopy());
		}
	}

	private void verifiesRequirementsVerifiedBy(TestCase source) {
		for (Requirement requirement : source.verifiedRequirements) {
			this.addVerifiedRequirement(requirement);
		}
	}

	private void setSimplePropertiesUsing(TestCase source) {
		this.setName(source.getName());
		this.setDescription(source.getDescription());
		this.executionMode = source.getExecutionMode();
		this.importance = source.getImportance();
	}

	public int getPositionOfStep(long stepId) throws UnknownEntityException {
		for (int i = 0; i < getSteps().size(); i++) {
			if (getSteps().get(i).getId() == stepId) {
				return i;
			}
		}

		throw new UnknownEntityException(stepId, TestStep.class);
	}

	@Override
	public String getClassSimpleName() {
		return TestCase.SIMPLE_CLASS_NAME;
	}

	@Override
	public String getClassName() {
		return TestCase.CLASS_NAME;
	}
	
	/**
	 * @return the weight
	 */
	public TestCaseImportance getImportance() {
		return importance;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setImportance(@NotNull TestCaseImportance weight) {
		this.importance = weight;
	}

	/**
	 * @return the weightAuto
	 */
	public boolean isImportanceAuto() {
		return importanceAuto;
	}

	/**
	 * @param importanceAuto the importanceAuto to set
	 */
	public void setImportanceAuto(boolean importanceAuto) {
		this.importanceAuto = importanceAuto;
	}

}
