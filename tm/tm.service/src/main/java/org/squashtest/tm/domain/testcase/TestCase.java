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
package org.squashtest.tm.domain.testcase;

import static org.squashtest.tm.domain.testcase.TestCaseImportance.LOW;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.exception.NoVerifiableRequirementVersionException;
import org.squashtest.tm.exception.UnallowedTestAssociationException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;

/**
 * @author Gregory Fouquet
 * 
 */
@Entity
@PrimaryKeyJoinColumn(name = "TCLN_ID")
public class TestCase extends TestCaseLibraryNode implements AttachmentHolder, BoundEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseLibraryNode.class);
	private static final String CLASS_NAME = "org.squashtest.tm.domain.testcase.TestCase";
	private static final String SIMPLE_CLASS_NAME = "TestCase";

	@Column(updatable = false)
	private final int version = 1;

	@NotNull
	private String reference = "";

	@Lob
	private String prerequisite = "";

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	private TestCaseExecutionMode executionMode = TestCaseExecutionMode.MANUAL;
	
	@NotNull
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "STEP_ORDER")
	@JoinTable(name = "TEST_CASE_STEPS", joinColumns = @JoinColumn(name = "TEST_CASE_ID"), inverseJoinColumns = @JoinColumn(name = "STEP_ID"))
	private final List<TestStep> steps = new ArrayList<TestStep>();

	@NotNull
	@OneToMany(cascade = { CascadeType.ALL})
	@JoinColumn(name="VERIFYING_TEST_CASE_ID")
	private Set<RequirementVersionCoverage> requirementVersionCoverages= new HashSet<RequirementVersionCoverage>();
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	private TestCaseImportance importance = LOW;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_NATURE")
	private TestCaseNature nature = TestCaseNature.UNDEFINED;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_TYPE")
	private TestCaseType type = TestCaseType.UNDEFINED;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_STATUS")
	private TestCaseStatus status = TestCaseStatus.WORK_IN_PROGRESS;
	
	
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
	
	
	/**
	 * @return {reference} - {name} if reference is not empty, or {name} if it is
	 * 
	 */
	public String getFullName(){
		if (StringUtils.isBlank(reference)){
			return getName();
		}
		else{
			return getReference()+" - "+getName();
		}
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
	 * @see TestCase#isAutomated()
	 * @return TODO either replaced by isAutomated or should be synchronized with isAutomated
	 */
	public TestCaseExecutionMode getExecutionMode() {
		return executionMode;
	}

	public List<TestStep> getSteps() {
		return steps;
	}
	
	
	//TODO : best would be to have a smarter subclass of List that would override #add(...) methods for this purpose
	private void notifyStepBelongsToMe(TestStep step){
		step.setTestCase(this);
	}

	public void addStep(@NotNull TestStep step) {
		getSteps().add(step);
		notifyStepBelongsToMe(step);
	}

	public void addStep(int index, @NotNull TestStep step) {
		getSteps().add(index, step);
		notifyStepBelongsToMe(step);
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

	
	@Override
	public void accept(TestCaseLibraryNodeVisitor visitor) {
		visitor.visit(this);

	}


	@Override
	public TestCase createCopy() {
		TestCase copy = new TestCase();
		copy.setSimplePropertiesUsing(this);
		copy.addCopiesOfSteps(this);
		copy.addCopiesOfAttachments(this);
		copy.verifiesRequirementsVerifiedBy(this);
		copy.notifyAssociatedWithProject(this.getProject());
		if(this.automatedTest != null){
			try{
			copy.setAutomatedTest(this.automatedTest);
			}catch(UnallowedTestAssociationException e){
				LOGGER.error("data inconsistancy : the test case #{} has a script even if it's project isn't test automation enabled", this.getId());
			}
		}
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

	

	private void setSimplePropertiesUsing(TestCase source) {
		this.setName(source.getName());
		this.setDescription(source.getDescription());
		this.setPrerequisite(source.getPrerequisite());
		this.executionMode = source.getExecutionMode();
		this.importance = source.getImportance();
		this.nature = source.getNature();
		this.type = source.getType();
		this.status = source.getStatus();
		this.reference = source.getReference();
	}

	/**
	 * Will compare id of test-case steps with given id and return the index of the matching step.
	 * Otherwise throw an exception.
	 * 
	 * @param stepId
	 * @return the step index (starting at 0)
	 * @throws UnknownEntityException
	 */
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

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}
	
	/**
	 * 
	 * @return the list of {@link ActionTestStep} or empty list
	 */
	public List<ActionTestStep> getActionSteps() {
		List<ActionTestStep> result = new ArrayList<ActionTestStep>();
		ActionStepRetreiver retriever = new ActionStepRetreiver(result);
		for(TestStep step : this.getSteps()){
			step.accept(retriever);
		}
		return retriever.getResult();
		
	}
	private final class ActionStepRetreiver implements TestStepVisitor{
		
		private List<ActionTestStep> result;
		
		private List<ActionTestStep> getResult(){
			return result;
		}

		private ActionStepRetreiver(List<ActionTestStep> result){
			this.result = result;			
		}
		@Override
		public void visit(ActionTestStep visited) {
			result.add(visited);
			
		}

		@Override
		public void visit(CallTestStep visited) {
			//noop
		}
		
	}
	
	//=====================Requirement verifying section====================
	
		
	/**
	 * 
	 * @return UNMODIFIABLE VIEW of verified requirements.
	 */
	public Set<RequirementVersion> getVerifiedRequirementVersions() {
		Set<RequirementVersion> verified = new HashSet<RequirementVersion>();
		for(RequirementVersionCoverage coverage : requirementVersionCoverages){
			verified.add(coverage.getVerifiedRequirementVersion());
		}
		return Collections.unmodifiableSet(verified);
	}


	/**
	 * Adds a {@link RequirementVersion} verified by this {@link RequirementVerifier}
	 * 
	 * @param requirementVersion
	 *            requirement to add, should not be null.
	 * @throws RequirementAlreadyVerifiedException
	 *             if this requirement verifier verifies another version of the same requirement
	 * @return the new {@link RequirementVersionCoverage}
	 */
	public RequirementVersionCoverage addVerifiedRequirementVersion(@NotNull RequirementVersion requirementVersion)
			throws RequirementAlreadyVerifiedException {
		
		checkRequirementNotVerified(requirementVersion);
		return forceAddVerifiedRequirement(requirementVersion);
	}

	/**
	 * This should be used when making a copy of a {@link RequirementVersion} to have the copy verified by this
	 * {@link RequirementVerifier}.
	 * 
	 * When making a copy of a requirement, we cannot use {@link #addVerifiedRequirementVersion(RequirementVersion)}
	 * because of the single requirement check.
	 * 
	 * @param requirementVersionCopy
	 *            a copy of an existing requirement version. It should not have a requirement yet.
	 * 
	 * @return the new {@link RequirementVersionCoverage}
	 */
	public RequirementVersionCoverage addCopyOfVerifiedRequirementVersion(RequirementVersion requirementVersionCopy) {
		if (requirementVersionCopy.getRequirement() != null) {
			throw new IllegalArgumentException("RequirementVersion should not be associated to a requirement yet");
		}
		return forceAddVerifiedRequirement(requirementVersionCopy);
	}

	/**
	 * @param version
	 * @throws RequirementAlreadyVerifiedException
	 */
	public void checkRequirementNotVerified(RequirementVersion version) throws RequirementAlreadyVerifiedException {
		Requirement req = version.getRequirement();

		for (RequirementVersion verified : getVerifiedRequirementVersions()) {
			if (req.equals(verified.getRequirement())) {
				throw new RequirementAlreadyVerifiedException(version, this);
			}
		}

	}
	/**
	 * 
	 * @param requirementVersion
	 * @return the new {@link RequirementVersionCoverage}
	 */
	private RequirementVersionCoverage forceAddVerifiedRequirement(RequirementVersion requirementVersion) {
		return new RequirementVersionCoverage(requirementVersion, this);		
	}
	/**
	 * Set the verifying test case as this, and add the coverage the the this.requirementVersionCoverage
	 * @param requirementVersionCoverage
	 */
	public void addRequirementCoverage(RequirementVersionCoverage requirementVersionCoverage) {
		requirementVersionCoverage.setVerifyingTestCase(this);
		this.requirementVersionCoverages.add(requirementVersionCoverage);		
	}

	/**
	 * This requirement verifier verifies the given requirement using its default verifiable version.
	 * 
	 * @param requirement
	 * @throws NoVerifiableRequirementVersionException
	 *             when there is no suitable version to be added
	 * @throws RequirementAlreadyVerifiedException
	 *             when this requirement verifier already verifies some version of the requirement.
	 */
	public void addVerifiedRequirement(@NotNull Requirement requirement)
			throws NoVerifiableRequirementVersionException, RequirementAlreadyVerifiedException {
		RequirementVersion candidate = requirement.getDefaultVerifiableVersion();
		addVerifiedRequirementVersion(candidate);		
	}
	
	private void verifiesRequirementsVerifiedBy(TestCase source) {
		for (RequirementVersion requirementVersion : source.getVerifiedRequirementVersions()) {
			if (requirementVersion.getStatus().isRequirementLinkable()) {
				this.addVerifiedRequirementVersion(requirementVersion);
			}
		}
	}

	public boolean hasStep(TestStep step) {
		for(TestStep step2 : steps){
			if(step2.getId().equals(step.getId())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Simply remove the RequirementVersionCoverage from this.requirementVersionCoverages.
	 * @param requirementVersionCoverage : the entity to remove from this test case's {@link RequirementVersionCoverage}s list.
	 */
	public void removeRequirementVersionCoverage(RequirementVersionCoverage requirementVersionCoverage) {
		this.requirementVersionCoverages.remove(requirementVersionCoverage);
		
	}
	
	/**
	 * Will return the list of this test-case's {@link RequirementVersionCoverage} that concerns {@link RequirementVersion} matching the given requirementVersionsIds.
	 * @param requirementVersionsIds : the ids of the verified {@link RequirementVersion}
	 * @return the list of corresponding {@link RequirementVersionCoverage}
	 */
	public List<RequirementVersionCoverage> findRequirementVersionCoverageForRequirements(List<Long> requirementVersionsIds) {
		List<RequirementVersionCoverage> result = new ArrayList<RequirementVersionCoverage>(requirementVersionsIds.size());
		for(RequirementVersionCoverage possibleMatch : this.requirementVersionCoverages){
			if(requirementVersionsIds.contains(possibleMatch.getVerifiedRequirementVersion().getId())){
				result.add(possibleMatch);
			}
		}
		return result;
	}
	/**
	 * Will return the {@link RequirementVersionCoverage} corresponding to the {@link RequirementVersion} matching the given id.
	 * @param requirementVersionId : the id of the concerned {@link RequirementVersion}
	 * @return the matching {@link RequirementVersionCoverage}
	 */
	public RequirementVersionCoverage findRequirementVersionCoverageForRequirement(long requirementVersionId) {
		for(RequirementVersionCoverage possibleMatch : this.requirementVersionCoverages){
			if(possibleMatch.getVerifiedRequirementVersion().getId().equals(requirementVersionId)){
				return possibleMatch;
			}
		}
		return null;
		
	}
	

}
