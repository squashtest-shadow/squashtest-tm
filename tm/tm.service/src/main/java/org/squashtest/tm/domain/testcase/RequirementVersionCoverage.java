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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;

/**
 * Entity representing a The coverage of a {@link RequirementVersion} by a {@link TestCase}.
 * The {@link ActionTestStep} responsible for the requirement coverage can be specified in the verifyingSteps property.
 * 
 * @author mpagnon
 *
 */
@NamedQueries({
	@NamedQuery(name="RequirementVersionCoverage.byRequirementVersionAndTestCase", query="select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where rv.id = :rvId and tc.id = :tcId"),
	@NamedQuery(name="RequirementVersionCoverage.byRequirementVersionAndTestCases", query="select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where rv.id = :rvId and tc.id in :tcIds"),
	@NamedQuery(name="RequirementVersionCoverage.byTestCaseAndRequirementVersions", query="select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where tc.id = :tcId and rv.id in :rvIds"),
})
@Entity
public class RequirementVersionCoverage implements Identified {
	@Id
	@GeneratedValue
	@Column(name = "REQUIREMENT_VERSION_COVERAGE_ID")
	private Long id;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "VERIFYING_TEST_CASE_ID", referencedColumnName = "TCLN_ID")
	private TestCase verifyingTestCase;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "VERIFIED_REQ_VERSION_ID", referencedColumnName = "RES_ID")
	private RequirementVersion verifiedRequirementVersion;
	
	@NotNull
	@OneToMany
	@JoinTable(name = "VERIFYING_STEPS", joinColumns = @JoinColumn(name = "REQUIREMENT_VERSION_COVERAGE_ID"), inverseJoinColumns = @JoinColumn(name = "TEST_STEP_ID"))
	private List<ActionTestStep> verifyingSteps = new ArrayList<ActionTestStep>();

	public TestCase getVerifyingTestCase() {
		return verifyingTestCase;
	}

	public void setVerifyingTestCase(TestCase verifyingTestCase) {
		if(this.verifiedRequirementVersion != null){
			verifyingTestCase.checkRequirementNotVerified(verifiedRequirementVersion);
		}
		this.verifyingTestCase = verifyingTestCase;
	}

	public RequirementVersion getVerifiedRequirementVersion() {
		return verifiedRequirementVersion;
	}

	public void setVerifiedRequirementVersion(RequirementVersion verifiedRequirementVersion) {
		if(this.verifyingTestCase != null){
			this.verifyingTestCase.checkRequirementNotVerified(verifiedRequirementVersion);
		}
		verifiedRequirementVersion.checkLinkable();
		this.verifiedRequirementVersion = verifiedRequirementVersion;
		
		
	}

	public Long getId() {
		return id;
	}

	public List<ActionTestStep> getVerifyingSteps() {
		return verifyingSteps;
	}
	
	public void addAllVerifyingSteps(Collection<ActionTestStep> steps){
		checkStepsBelongToTestCase(steps);
		this.verifyingSteps.addAll(steps);
	}
	
	private void checkStepsBelongToTestCase(Collection<ActionTestStep> steps) {
		for(ActionTestStep step : steps){
			if(verifyingTestCase.hasStep(step)){
				//TODO throw StepDoesNotBelongToTestCaseException;
			}
		}
		
	}
	
	RequirementVersionCoverage(){
		super();
	}

	private RequirementVersionCoverage(TestCase verifyingTestCase) {
		super();
		this.verifyingTestCase = verifyingTestCase;
		verifyingTestCase.addRequirementCoverage(this);
	}
	
	/**
	 * @throws RequirementVersionNotLinkableException
	 * @param verifiedRequirementVersion
	 */
	public RequirementVersionCoverage(RequirementVersion verifiedRequirementVersion) {
		super();
		verifiedRequirementVersion.checkLinkable();
		this.verifiedRequirementVersion = verifiedRequirementVersion;
		verifiedRequirementVersion.addRequirementCoverage(this);
	}
	
	

	/**
	 * @throws RequirementAlreadyVerifiedException
	 * @throws RequirementVersionNotLinkableException
	 * @param requirementVersion
	 * @param testCase
	 */
	public RequirementVersionCoverage(RequirementVersion requirementVersion, TestCase testCase) {
		//check
		testCase.checkRequirementNotVerified(requirementVersion);
		requirementVersion.checkLinkable();
		//set
		testCase.addRequirementCoverage(this);
		this.verifyingTestCase = testCase;		
		requirementVersion.addRequirementCoverage(this);
		this.verifiedRequirementVersion = requirementVersion;
	}
	/**
	 * @throws RequirementAlreadyVerifiedException
	 * @throws RequirementVersionNotLinkableException
	 * @param requirement
	 * @param testCase
	 */
	public RequirementVersionCoverage(Requirement requirement, TestCase testCase) {
		//check
		testCase.checkRequirementNotVerified(requirement.getCurrentVersion());
		requirement.getCurrentVersion().checkLinkable();
		//set
		testCase.addRequirementCoverage(this);
		this.verifyingTestCase = testCase;		
		requirement.getCurrentVersion().addRequirementCoverage(this);
		this.verifiedRequirementVersion = requirement.getCurrentVersion();
	}


	public RequirementVersionCoverage copyForRequirementVersion(RequirementVersion rvCopy){
		RequirementVersionCoverage rvcCopy = new RequirementVersionCoverage(this.verifyingTestCase);
		rvcCopy.addAllVerifyingSteps(this.verifyingSteps);
		rvcCopy.setVerifiedRequirementVersion(rvCopy);
		return rvcCopy;
	}

	public RequirementVersionCoverage copyForTestCase(TestCase tcCopy){
		//copy verified requirement
		RequirementVersionCoverage rvcCopy = new RequirementVersionCoverage(this.verifiedRequirementVersion);
		// set verifying test case
		rvcCopy.setVerifyingTestCase(tcCopy);
		//set verifying steps
		List<ActionTestStep> stepToVerify = new ArrayList<ActionTestStep>(this.verifyingSteps.size());
		for(ActionTestStep step : this.verifyingSteps){
			int indexInSource = this.verifyingTestCase.getPositionOfStep(step.getId());
			stepToVerify.add((ActionTestStep)tcCopy.getSteps().get(indexInSource));
		}
		rvcCopy.addAllVerifyingSteps(stepToVerify);		
		return rvcCopy;
	}

	/**
	 * @throws RequirementVersionNotLinkableException
	 */
	public void checkDeletable() {
		this.verifiedRequirementVersion.checkLinkable();
	}
	
	
}
