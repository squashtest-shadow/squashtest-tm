/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.search.annotations.DocumentId;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.testcase.StepDoesNotBelongToTestCaseException;

/**
 * Entity representing a The coverage of a {@link RequirementVersion} by a {@link TestCase}. The {@link ActionTestStep}
 * responsible for the requirement coverage can be specified in the verifyingSteps property.
 * 
 * @author mpagnon
 * 
 */
@NamedQueries({
		@NamedQuery(name = "RequirementVersionCoverage.byRequirementVersionAndTestCase", query = "select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where rv.id = :rvId and tc.id = :tcId"),
		@NamedQuery(name = "RequirementVersionCoverage.byRequirementVersionAndTestCases", query = "select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where rv.id = :rvId and tc.id in :tcIds"),
		@NamedQuery(name = "RequirementVersionCoverage.byTestCaseAndRequirementVersions", query = "select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where tc.id = :tcId and rv.id in :rvIds"),
		@NamedQuery(name = "RequirementVersionCoverage.numberByTestCase", query = "select count(rvc) from RequirementVersionCoverage rvc join rvc.verifyingTestCase tc where tc.id = :tcId"),
		@NamedQuery(name = "RequirementVersionCoverage.numberByTestCases", query = "select count(rvc) from RequirementVersionCoverage rvc join rvc.verifyingTestCase tc where tc.id in :tcIds"),
		@NamedQuery(name = "RequirementVersionCoverage.numberDistinctVerifiedByTestCases", query = "select count(distinct rv) from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where tc.id in :tcIds"),
		@NamedQuery(name = "RequirementVersionCoverage.byRequirementVersionsAndTestStep", query = "select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingSteps step where step.id = :stepId and rv.id in :rvIds"), })
@Entity
public class RequirementVersionCoverage implements Identified {
	@Id
	@GeneratedValue
	@Column(name = "REQUIREMENT_VERSION_COVERAGE_ID")
	@DocumentId
	private Long id;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "VERIFYING_TEST_CASE_ID", referencedColumnName = "TCLN_ID")
	private TestCase verifyingTestCase;

	@ManyToOne
	@JoinColumn(name = "VERIFIED_REQ_VERSION_ID", referencedColumnName = "RES_ID")
	private RequirementVersion verifiedRequirementVersion;

	@NotNull
	@ManyToMany(mappedBy="requirementVersionCoverages")
	private Set<ActionTestStep> verifyingSteps = new HashSet<ActionTestStep>();

	/**
	 * @throws RequirementVersionNotLinkableException
	 * @param verifiedRequirementVersion
	 */
	public RequirementVersionCoverage(RequirementVersion verifiedRequirementVersion) {
		this(verifiedRequirementVersion, null);
	}

	/**
	 * @throws RequirementAlreadyVerifiedException
	 * @throws RequirementVersionNotLinkableException
	 * @param requirementVersion
	 * @param testCase
	 */
	public RequirementVersionCoverage(RequirementVersion requirementVersion, TestCase testCase) {
		// check - these can throw exception (not so good a practice) so they **must** be performed before we change the passed args state 
		requirementVersion.checkLinkable();
		if (testCase != null) {
			testCase.checkRequirementNotVerified(requirementVersion);
		}
		
		// set
		this.verifiedRequirementVersion = requirementVersion;
		verifiedRequirementVersion.addRequirementCoverage(this);
		if (testCase != null) {
			testCase.addRequirementCoverage(this);
			this.verifyingTestCase = testCase;
		}
	}

	/**
	 * @throws RequirementAlreadyVerifiedException
	 * @throws RequirementVersionNotLinkableException
	 * @param requirement
	 * @param testCase
	 */
	public RequirementVersionCoverage(Requirement requirement, TestCase testCase) {
		this(requirement.getCurrentVersion(), testCase);
	}

	public TestCase getVerifyingTestCase() {
		return verifyingTestCase;
	}

	public void setVerifyingTestCase(TestCase verifyingTestCase) {
		if (this.verifiedRequirementVersion != null) {
			verifyingTestCase.checkRequirementNotVerified(this, verifiedRequirementVersion);
		}
		this.verifyingTestCase = verifyingTestCase;
	}

	public RequirementVersion getVerifiedRequirementVersion() {
		return verifiedRequirementVersion;
	}

	public void setVerifiedRequirementVersion(RequirementVersion verifiedRequirementVersion) {
		if (this.verifyingTestCase != null && this.verifiedRequirementVersion != null) {
			this.verifyingTestCase.checkRequirementNotVerified(this, verifiedRequirementVersion);
		}
		verifiedRequirementVersion.checkLinkable();
		this.verifiedRequirementVersion = verifiedRequirementVersion;
		verifiedRequirementVersion.addRequirementCoverage(this);

	}

	public Long getId() {
		return id;
	}

	public Set<ActionTestStep> getVerifyingSteps() {
		return verifyingSteps;
	}

	/**
	 * Checks that all steps belong to this {@linkplain RequirementVersionCoverage#verifyingTestCase} and add them to
	 * this {@linkplain RequirementVersionCoverage#verifyingSteps}.
	 * 
	 * @param steps
	 * @throws StepDoesNotBelongToTestCaseException
	 * 
	 */
	public void addAllVerifyingSteps(Collection<ActionTestStep> steps) {
		checkStepsBelongToTestCase(steps);

		this.verifyingSteps.addAll(steps);
		for (ActionTestStep step : steps) {
			step.addRequirementVersionCoverage(this);
		}
	}

	/**
	 * Will check that all steps are found in this.verifyingTestCase.steps. The check is with
	 * {@link TestCase#hasStep(TestStep)}
	 * 
	 * @param steps
	 * @throws StepDoesNotBelongToTestCaseException
	 *             if one step doesn't belong to this.verifyingTestCase.
	 */
	private void checkStepsBelongToTestCase(Collection<ActionTestStep> steps) {
		for (ActionTestStep step : steps) {
			if (!verifyingTestCase.hasStep(step)) {
				throw new StepDoesNotBelongToTestCaseException(verifyingTestCase.getId(), step.getId());
			}
		}

	}

	RequirementVersionCoverage() {
		super();
	}

	private RequirementVersionCoverage(TestCase verifyingTestCase) {
		super();
		this.verifyingTestCase = verifyingTestCase;
		verifyingTestCase.addRequirementCoverage(this);
	}

	public RequirementVersionCoverage copyForRequirementVersion(RequirementVersion rvCopy) {
		RequirementVersionCoverage rvcCopy = new RequirementVersionCoverage(this.verifyingTestCase);
		rvcCopy.addAllVerifyingSteps(this.verifyingSteps);
		rvcCopy.setVerifiedRequirementVersion(rvCopy);
		return rvcCopy;
	}

	public RequirementVersionCoverage copyForTestCase(TestCase tcCopy) {
		// copy verified requirement
		RequirementVersionCoverage rvcCopy = new RequirementVersionCoverage(this.verifiedRequirementVersion);
		// set verifying test case
		rvcCopy.setVerifyingTestCase(tcCopy);
		tcCopy.addRequirementCoverage(rvcCopy);
		// set verifying steps
		List<ActionTestStep> stepToVerify = new ArrayList<ActionTestStep>(this.verifyingSteps.size());
		for (ActionTestStep step : this.verifyingSteps) {
			int indexInSource = this.verifyingTestCase.getPositionOfStep(step.getId());
			stepToVerify.add((ActionTestStep) tcCopy.getSteps().get(indexInSource));
		}
		rvcCopy.addAllVerifyingSteps(stepToVerify);
		return rvcCopy;
	}

	/**
	 * @throws RequirementVersionNotLinkableException
	 */
	public void checkCanRemoveTestCaseFromRequirementVersion() {
		this.verifiedRequirementVersion.checkLinkable();
	}

	/**
	 * Returns true if the given step id matches on of the verifying steps id.
	 * 
	 * @param stepId
	 * @return
	 */
	public boolean hasStepAsVerifying(long stepId) {
		for (ActionTestStep step : this.verifyingSteps) {
			if (step.getId().equals(stepId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Will remove the step matching the given id from this.verifyingSteps. If the step is not found nothing special
	 * happens.
	 * 
	 * @param testStepId
	 *            : the id of the step to remove.
	 */
	public void removeVerifyingStep(long testStepId) {
		Iterator<ActionTestStep> iterator = this.verifyingSteps.iterator();
		while (iterator.hasNext()) {
			ActionTestStep step = iterator.next();
			if (step.getId().equals(testStepId)) {
				iterator.remove();
			}
		}

	}

}
