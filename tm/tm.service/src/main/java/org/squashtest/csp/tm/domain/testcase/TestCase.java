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
package org.squashtest.csp.tm.domain.testcase;

import static org.squashtest.csp.tm.domain.testcase.TestCaseImportance.LOW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.NoVerifiableRequirementVersionException;
import org.squashtest.csp.tm.domain.RequirementAlreadyVerifiedException;
import org.squashtest.csp.tm.domain.UnknownEntityException;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.audit.AuditableMixin;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.exception.UnallowedTestAssociationException;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testautomation.AutomatedTest;

/**
 * @author Gregory Fouquet
 * 
 */
@Entity
@PrimaryKeyJoinColumn(name = "TCLN_ID")
public class TestCase extends TestCaseLibraryNode implements AttachmentHolder, BoundEntity {
	private static final String CLASS_NAME = "org.squashtest.csp.tm.domain.testcase.TestCase";
	private static final String SIMPLE_CLASS_NAME = "TestCase";

	@Column(updatable = false)
	private final int version = 1;

	private String reference = "";

	@Lob
	private String prerequisite = "";

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	private TestCaseExecutionMode executionMode = TestCaseExecutionMode.MANUAL;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "STEP_ORDER")
	@JoinTable(name = "TEST_CASE_STEPS", joinColumns = @JoinColumn(name = "TEST_CASE_ID"), inverseJoinColumns = @JoinColumn(name = "STEP_ID"))
	private final List<TestStep> steps = new ArrayList<TestStep>();

	/**
	 * Collection of {@link RequirementVersion}s which are verified by this {@link TestCase}
	 */
	@ManyToMany
	@JoinTable(name = "TEST_CASE_VERIFIED_REQUIREMENT_VERSION", joinColumns = @JoinColumn(name = "VERIFYING_TEST_CASE_ID"), inverseJoinColumns = @JoinColumn(name = "VERIFIED_REQ_VERSION_ID"))
	private final Set<RequirementVersion> verifiedRequirementVersions = new HashSet<RequirementVersion>();

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	private TestCaseImportance importance = LOW;

	/**
	 * Should the importance be automatically computed.
	 */
	private boolean importanceAuto = false;

	@ManyToOne
	@JoinColumn(name = "TA_TEST")
	private AutomatedTest automatedTest;

	// *************************** CODE *************************************

	public TestCase(Date createdOn, String createdBy) {
		AuditableMixin audit = ((AuditableMixin) this);

		audit.setCreatedOn(createdOn);
		audit.setCreatedBy(createdBy);
	}

	public TestCase() {
		super();
	}

	public int getVersion() {
		return version;
	}

	/***
	 * @return the reference of the test-case
	 */
	public String getReference() {
		return reference;
	}

	/***
	 * Set the test-case reference
	 * 
	 * @param reference
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getPrerequisite() {
		return prerequisite;
	}

	/**
	 * @see #isAutomated()
	 * @param exectionMode
	 * @deprecated not set anymore in app
	 */
	@Deprecated
	public void setExecutionMode(TestCaseExecutionMode exectionMode) {
		this.executionMode = exectionMode;
	}

	/**
	 * @see TestCase#isAutomated()
	 * @return TODO either replaced by isAutomated or should be synchronized with isAutomated
	 */
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
	 * @param newIndex
	 *            the position we want the first element of movedSteps to be once the operation is complete
	 * @param movedSteps
	 *            the list of steps to move, sorted by rank among each others.
	 */
	public void moveSteps(int newIndex, List<TestStep> movedSteps) {
		if (!steps.isEmpty()) {
			steps.removeAll(movedSteps);
			steps.addAll(newIndex, movedSteps);
		}
	}

	/**
	 * 
	 * @return UNMODIFIABLE VIEW of verified requirements.
	 */
	public Set<RequirementVersion> getVerifiedRequirementVersions() {
		return Collections.unmodifiableSet(verifiedRequirementVersions);
	}

	@Override
	public void accept(TestCaseLibraryNodeVisitor visitor) {
		visitor.visit(this);

	}

	/**
	 * Adds a {@link RequirementVersion} verified by this {@link TestCase}
	 * 
	 * @param requirementVersion
	 *            requirement to add, should not be null.
	 * @throws RequirementAlreadyVerifiedException
	 *             if this test case already verifies another version of the same requirment
	 */
	public void addVerifiedRequirementVersion(@NotNull RequirementVersion requirementVersion)
			throws RequirementAlreadyVerifiedException {
		checkRequirementNotVerified(requirementVersion);
		forceAddVerifiedRequirement(requirementVersion);
	}

	/**
	 * This should be used when making a copy of a {@link RequirementVersion} to have the copy verified by this
	 * {@link TestCase}.
	 * 
	 * When making a copy of a requirement, we cannot use {@link #addVerifiedRequirementVersion(RequirementVersion)}
	 * because of the single requirment check.
	 * 
	 * @param requirementVersionCopy
	 *            a copy of an existing requirement version. It should not have a requirement yet.
	 */
	public void addCopyOfVerifiedRequirementVersion(RequirementVersion requirementVersionCopy) {
		if (requirementVersionCopy.getRequirement() != null) {
			throw new IllegalArgumentException("RequirementVersion should not be associated to a requirement yet");
		}

		forceAddVerifiedRequirement(requirementVersionCopy);
	}

	private void forceAddVerifiedRequirement(RequirementVersion requirementVersionCopy) {
		requirementVersionCopy.notifyVerifiedBy(this);
		verifiedRequirementVersions.add(requirementVersionCopy);
	}

	/**
	 * @param version
	 * @throws RequirementAlreadyVerifiedException
	 */
	public void checkRequirementNotVerified(RequirementVersion version) throws RequirementAlreadyVerifiedException {
		Requirement req = version.getRequirement();

		for (RequirementVersion verified : verifiedRequirementVersions) {
			if (req.equals(verified.getRequirement())) {
				throw new RequirementAlreadyVerifiedException(version, this);
			}
		}

	}

	public void removeVerifiedRequirementVersion(@NotNull RequirementVersion requirement) {
		requirement.notifyNoLongerVerifiedBy(this);
		verifiedRequirementVersions.remove(requirement);
	}

	@Override
	public TestCase createPastableCopy() {
		TestCase copy = new TestCase();
		copy.setSimplePropertiesUsing(this);
		copy.addCopiesOfSteps(this);
		copy.addCopiesOfAttachments(this);
		copy.verifiesRequirementsVerifiedBy(this);
		copy.notifyAssociatedWithProject(this.getProject());

		return copy;
	}

	private void addCopiesOfAttachments(TestCase source) {
		for (Attachment tcAttach : source.getAttachmentList().getAllAttachments()) {
			Attachment atCopy = tcAttach.hardCopy();
			this.getAttachmentList().addAttachment(atCopy);
		}
	}

	private void addCopiesOfSteps(TestCase source) {
		for (TestStep testStep : source.getSteps()) {
			this.addStep(testStep.createCopy());
		}
	}

	private void verifiesRequirementsVerifiedBy(TestCase source) {
		for (RequirementVersion requirement : source.verifiedRequirementVersions) {
			if (requirement.getStatus().isRequirementLinkable()) {
				this.addVerifiedRequirementVersion(requirement);
			}
		}
	}

	private void setSimplePropertiesUsing(TestCase source) {
		this.setName(source.getName());
		this.setDescription(source.getDescription());
		this.setPrerequisite(source.getPrerequisite());
		this.executionMode = source.getExecutionMode();
		this.importance = source.getImportance();
		this.reference = source.getReference();
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
	 * @param weight
	 *            the weight to set
	 */
	public void setImportance(@NotNull TestCaseImportance weight) {
		this.importance = weight;
	}

	/**
	 * @param prerequisite
	 *            the prerequisite to set
	 */
	public void setPrerequisite(@NotNull String prerequisite) {
		this.prerequisite = prerequisite;
	}

	/**
	 * @return the weightAuto
	 */
	public boolean isImportanceAuto() {
		return importanceAuto;
	}

	/**
	 * @param importanceAuto
	 *            the importanceAuto to set
	 */
	public void setImportanceAuto(boolean importanceAuto) {
		this.importanceAuto = importanceAuto;
		// if (importanceAuto) {
		// The calculation of importance when auto is on is not done here because it needs
		// to know the call-steps associated requirements.
		// }
	}

	/**
	 * This test case verifies the given requirement using its default verifiable version.
	 * 
	 * @param requirement
	 * @throws NoVerifiableRequirementVersionException
	 *             when there is no suitable version to be added
	 * @throws RequirementAlreadyVerifiedException
	 *             when this test case already verifies some version of the requirement.
	 */
	public void addVerifiedRequirement(@NotNull Requirement requirement)
			throws NoVerifiableRequirementVersionException, RequirementAlreadyVerifiedException {
		RequirementVersion candidate = requirement.getDefaultVerifiableVersion();
		addVerifiedRequirementVersion(candidate);
	}

	// *************** test automation section ******************

	public AutomatedTest getAutomatedTest() {
		return automatedTest;
	}

	public void setAutomatedTest(AutomatedTest testAutomationTest) {
		if (getProject().isTestAutomationEnabled()) {
			this.automatedTest = testAutomationTest;
		} else {
			throw new UnallowedTestAssociationException();
		}
	}
	
	public void removeAutomatedScript() {
		this.automatedTest = null;
	}

	public boolean isAutomated() {
		return (automatedTest != null && getProject().isTestAutomationEnabled());
	}
	
	// ***************** (detached) custom field section *************
	
	@Override
	public Long getBoundEntityId() {
		return getId();
	}
	
	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.TEST_CASE;
	}
	


}
