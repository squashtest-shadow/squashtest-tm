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
package org.squashtest.tm.domain.testcase;

import static org.squashtest.tm.domain.testcase.TestCaseImportance.LOW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.persistence.OrderBy;
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
import org.squashtest.tm.exception.UnallowedTestAssociationException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.exception.customfield.NameAlreadyInUseException;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.Index;

/**
 * @author Gregory Fouquet
 * 
 */
@Entity
@Indexed
@ClassBridge(
		name="nb_requirements",
		store=Store.YES,
		impl=TestCaseBridgeRequirements.class)
@PrimaryKeyJoinColumn(name = "TCLN_ID")
public class TestCase extends TestCaseLibraryNode implements AttachmentHolder, BoundEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseLibraryNode.class);
	private static final String CLASS_NAME = "org.squashtest.tm.domain.testcase.TestCase";
	private static final String SIMPLE_CLASS_NAME = "TestCase";

	@Column(updatable = false)
	private final int version = 1;

	@NotNull
	@Field
	private String reference = "";

	@Lob
	@Field
	private String prerequisite = "";

	@NotNull
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "STEP_ORDER")
	@JoinTable(name = "TEST_CASE_STEPS", joinColumns = @JoinColumn(name = "TEST_CASE_ID"), inverseJoinColumns = @JoinColumn(name = "STEP_ID"))
	private final List<TestStep> steps = new ArrayList<TestStep>();

	@NotNull
	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE })
	@JoinColumn(name = "VERIFYING_TEST_CASE_ID")
	//@ContainedIn
	private Set<RequirementVersionCoverage> requirementVersionCoverages = new HashSet<RequirementVersionCoverage>(0);

	@NotNull
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "testCase")
	@OrderBy("name")
	@Field
	@FieldBridge(impl = TestCaseCountParametersBridge.class)
	private Set<Parameter> parameters = new HashSet<Parameter>(0);

	@NotNull
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "testCase")
	@OrderBy("name")
	@Field
	@FieldBridge(impl = TestCaseCountDatasetsBridge.class)
	private Set<Dataset> datasets = new HashSet<Dataset>(0);

	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional=false)
	@Field(analyze=Analyze.NO, store=Store.YES)
	private TestCaseImportance importance = LOW;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_NATURE")
	@Field(analyze=Analyze.NO, store=Store.YES)
	private TestCaseNature nature = TestCaseNature.UNDEFINED;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_TYPE")
	@Field(analyze=Analyze.NO, store=Store.YES)
	private TestCaseType type = TestCaseType.UNDEFINED;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_STATUS")
	@Field(analyze=Analyze.NO, store=Store.YES)
	private TestCaseStatus status = TestCaseStatus.WORK_IN_PROGRESS;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "EXECUTION_MODE")
	private TestCaseExecutionMode executionMode = TestCaseExecutionMode.MANUAL;

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
	public String getFullName() {
		if (StringUtils.isBlank(reference)) {
			return getName();
		} else {
			return getReference() + " - " + getName();
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

	public void setExecutionMode(@NotNull TestCaseExecutionMode executionMode) {
		this.executionMode = executionMode;
	}

	
	public List<TestStep> getSteps() {
		return steps;
	}

	// TODO : best would be to have a smarter subclass of List that would override #add(...) methods for this purpose
	private void notifyStepBelongsToMe(TestStep step) {
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

	/**
	 * Will create a copy from this instance. <br>
	 * Will copy all properties, steps, automated scripts, parameters, datasets.<br>
	 * ! Will not copy {@link RequirementVersionCoverage}s !
	 * 
	 * @return a copy of this {@link TestCase}
	 */
	@Override
	public TestCase createCopy() {
		TestCase copy = new TestCase();
		copy.setSimplePropertiesUsing(this);
		copy.addCopiesOfSteps(this);
		copy.addCopiesOfAttachments(this);
		copy.addCopiesOfParametersAndDatasets(this);
		copy.notifyAssociatedWithProject(this.getProject());
		if (this.automatedTest != null) {
			try {
				copy.setAutomatedTest(this.automatedTest);
			} catch (UnallowedTestAssociationException e) {
				LOGGER.error(
						"data inconsistancy : this test case (#{}) has a script even if it's project isn't test automation enabled",
						this.getId());
			}
		}
		return copy;
	}

	/**
	 * will add to this parameters, datasets and dataParamValues copied from the given source.
	 * 
	 * @param source
	 *            : the source test case to copy the params, datasets and dataparamvalues from.
	 */
	private void addCopiesOfParametersAndDatasets(TestCase source) {
		// create copy of parameters and remember the association original/copy
		Map<Parameter, Parameter> copyByOriginalParam = new HashMap<Parameter, Parameter>(source.getParameters().size());
		for (Parameter parameter : source.getParameters()) {
			Parameter paramCopy = new Parameter(parameter.getName(), this);
			copyByOriginalParam.put(parameter, paramCopy);
		}
		addCopiesOfDatasets(source, copyByOriginalParam);
	}

	private void addCopiesOfDatasets(TestCase source, Map<Parameter, Parameter> copyByOriginalParam) {
		for (Dataset dataset : source.getDatasets()) {
			Dataset datasetCopy = new Dataset(dataset.getName(), this);
			// create copy of datasetParamValues and link the copies to the rightful parameters
			for (DatasetParamValue datasetParamValue : dataset.getParameterValues()) {
				Parameter datasetParamValueCopyParam = getParameterToLinkedTheCopiedDatasetParamValueTo(source,
						copyByOriginalParam, datasetParamValue);
				String datasetParamValueCopyParamValue = datasetParamValue.getParamValue();
				new DatasetParamValue(datasetParamValueCopyParam, datasetCopy, datasetParamValueCopyParamValue);
			}
		}
	}

	private Parameter getParameterToLinkedTheCopiedDatasetParamValueTo(TestCase source,
			Map<Parameter, Parameter> copyByOriginalParam, DatasetParamValue datasetParamValue) {
		Parameter datasetParamValueCopyParam;
		if (datasetParamValue.getParameter().getTestCase().getId().equals(source.getId())) {
			// if the parameter associated to the datasetParamValue is from this test case we need to link the
			// copied paramValue to the copy of the parameter
			datasetParamValueCopyParam = copyByOriginalParam.get(datasetParamValue.getParameter());
		} else {
			// if the parameter associated to the datasetParamValue belongs to a called test-case we link the
			// copied paramValue to the same parameter it's source is.
			datasetParamValueCopyParam = datasetParamValue.getParameter();
		}
		return datasetParamValueCopyParam;
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
	 * Will compare id of test-case steps with given id and return the index of the matching step. Otherwise throw an
	 * exception.
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
		for (TestStep step : this.getSteps()) {
			step.accept(retriever);
		}
		return retriever.getResult();

	}

	private static final class ActionStepRetreiver implements TestStepVisitor {

		private List<ActionTestStep> result;

		private List<ActionTestStep> getResult() {
			return result;
		}

		private ActionStepRetreiver(List<ActionTestStep> result) {
			this.result = result;
		}

		@Override
		public void visit(ActionTestStep visited) {
			result.add(visited);

		}

		@Override
		public void visit(CallTestStep visited) {
			// noop
		}

	}

	// =====================Requirement verifying section====================

	/**
	 * 
	 * @return UNMODIFIABLE VIEW of verified requirements.
	 */
	public Set<RequirementVersion> getVerifiedRequirementVersions() {
		Set<RequirementVersion> verified = new HashSet<RequirementVersion>();
		for (RequirementVersionCoverage coverage : requirementVersionCoverages) {
			verified.add(coverage.getVerifiedRequirementVersion());
		}
		return Collections.unmodifiableSet(verified);
	}

	/**
	 * 
	 * Checks if the given version is already verified, avoiding to look at the given requirementVersionCoverage.
	 * 
	 * @param requirementVersionCoverage
	 * @param version
	 * @throws RequirementAlreadyVerifiedException
	 */
	public void checkRequirementNotVerified(RequirementVersionCoverage requirementVersionCoverage,
			RequirementVersion version) throws RequirementAlreadyVerifiedException {
		Requirement req = version.getRequirement();
		for (RequirementVersionCoverage coverage : this.requirementVersionCoverages) {
			if (coverage != requirementVersionCoverage) {
				RequirementVersion verified = coverage.getVerifiedRequirementVersion();
				if (verified != null && req.equals(verified.getRequirement())) {
					throw new RequirementAlreadyVerifiedException(version, this);
				}
			}
		}

	}

	/**
	 * Set the verifying test case as this, and add the coverage the the this.requirementVersionCoverage
	 * 
	 * @param requirementVersionCoverage
	 */
	public void addRequirementCoverage(RequirementVersionCoverage requirementVersionCoverage) {
		this.requirementVersionCoverages.add(requirementVersionCoverage);
	}

	/**
	 * Copy this.requirementVersionCoverages . All {@link RequirementVersionCoverage} having for verifying test case the
	 * copy param.
	 * 
	 * @param copy
	 *            : the {@link TestCase} that will verify the copied coverages
	 * @return : the copied {@link RequirementVersionCoverage}s
	 */
	public List<RequirementVersionCoverage> createRequirementVersionCoveragesForCopy(TestCase copy) {
		List<RequirementVersionCoverage> createdCoverages = new ArrayList<RequirementVersionCoverage>();
		for (RequirementVersionCoverage coverage : this.requirementVersionCoverages) {
			createdCoverages.add(coverage.copyForTestCase(copy));
		}
		return createdCoverages;
	}

	/**
	 * Returns true if a step of the same id is found in this.steps.
	 * 
	 * @param step
	 *            : the step to check
	 * @return true if this {@link TestCase} has the given step.
	 */
	public boolean hasStep(TestStep step) {
		for (TestStep step2 : steps) {
			if (step2.getId().equals(step.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Simply remove the RequirementVersionCoverage from this.requirementVersionCoverages.
	 * 
	 * @param requirementVersionCoverage
	 *            : the entity to remove from this test case's {@link RequirementVersionCoverage}s list.
	 */
	public void removeRequirementVersionCoverage(RequirementVersionCoverage requirementVersionCoverage) {
		this.requirementVersionCoverages.remove(requirementVersionCoverage);

	}

	/***
	 * 
	 * @return an unmodifiable set of the test case {@link RequirementVersionCoverage}s
	 */
	public Set<RequirementVersionCoverage> getRequirementVersionCoverages() {
		return Collections.unmodifiableSet(this.requirementVersionCoverages);
	}

	/**
	 * @param calledVersion
	 * @return true if this {@link TestCase} verifies the {@link RequirementVersion}
	 */
	public boolean verifies(RequirementVersion rVersion) {
		for (RequirementVersionCoverage coverage : this.requirementVersionCoverages) {
			if (coverage.getVerifiedRequirementVersion().getId().equals(rVersion.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * checks if the given version is already verified.
	 * 
	 * @param version
	 * @throws RequirementAlreadyVerifiedException
	 */
	public void checkRequirementNotVerified(RequirementVersion version) throws RequirementAlreadyVerifiedException {
		Requirement req = version.getRequirement();
		for (RequirementVersion verified : this.getVerifiedRequirementVersions()) {
			if (verified != null && req.equals(verified.getRequirement())) {
				throw new RequirementAlreadyVerifiedException(version, this);
			}
		}

	}

	// =====================Parameter Section====================
	public Set<Parameter> getParameters() {
		return Collections.unmodifiableSet(this.parameters);
	}

	/**
	 * If the given parameter doesn't already exists in this.parameters, and, if the given parameter's name is not found
	 * in this.parmeters : will add the given parameter to this.parameters.
	 * 
	 * @throws NameAlreadyInUseException
	 * @param parameter
	 */
	protected void addParameter(@NotNull Parameter parameter) {
		Parameter homonyme = findParameterByName(parameter.getName());
		if (homonyme != null && !homonyme.equals(parameter)) {
				throw new NameAlreadyInUseException(Parameter.class.getSimpleName(), parameter.getName());
		}
		this.parameters.add(parameter);

	}

	public Set<Dataset> getDatasets() {
		return Collections.unmodifiableSet(this.datasets);
	}

	public void addDataset(@NotNull Dataset dataset) {
		this.datasets.add(dataset);
	}

	/**
	 * Will go through this.parameters and return the Parameter matching the given name
	 * 
	 * @param name
	 *            : the name of the parameter to return
	 * @return the parameter matching the given name or <code>null</code>
	 */
	public Parameter findParameterByName(String name) {
		for (Parameter parameter : this.parameters) {
			if (parameter.getName().equals(name)) {
				return parameter;
			}
		}
		return null;
	}

	/**
	 * Will find the names of all parameters used in this test case's steps.
	 * 
	 * @return a Set of Sting empty or containing all used parameter names in this steps.
	 */
	public Set<String> findUsedParamsNamesInSteps() {
		Set<String> result = new HashSet<String>();
		for (ActionTestStep step : this.getActionSteps()) {
			result.addAll(step.findUsedParametersNames());
		}
		return result;
	}

}
