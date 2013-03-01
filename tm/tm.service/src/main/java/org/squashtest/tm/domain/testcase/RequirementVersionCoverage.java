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
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * Entity representing a The coverage of a {@link RequirementVersion} by a {@link TestCase}.
 * The {@link ActionTestStep} responsible for the requirement coverage can be specified in the verifyingSteps property.
 * 
 * @author mpagnon
 *
 */
@NamedQueries({
	@NamedQuery(name="RequirementVersionCoverage.findByRequirementVersionAndTestCase", query="select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where rv.id = :rvId and tc.id = :tcId"),
	@NamedQuery(name="RequirementVersionCoverage.findAllByRequirementVersionAndTestCases", query="select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where rv.id = :rvId and tc.id in :tcIds"),
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
		this.verifyingTestCase = verifyingTestCase;
	}

	public RequirementVersion getVerifiedRequirementVersion() {
		return verifiedRequirementVersion;
	}

	public void setVerifiedRequirementVersion(RequirementVersion verifiedRequirementVersion) {
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
	
	public RequirementVersionCoverage(){
		super();
	}

	public RequirementVersionCoverage(TestCase verifyingTestCase) {
		super();
		verifyingTestCase.addRequirementCoverage(this);
	}
	
	public RequirementVersionCoverage(RequirementVersion verifiedRequirementVersion) {
		super();
		verifiedRequirementVersion.addRequirementCoverage(this);
	}

	public RequirementVersionCoverage(RequirementVersion requirementVersion, TestCase testCase) {
		testCase.addRequirementCoverage(this);
		requirementVersion.addRequirementCoverage(this);
	}

	public RequirementVersionCoverage copyVerifying(){
		RequirementVersionCoverage copy = new RequirementVersionCoverage(this.verifyingTestCase);
		copy.addAllVerifyingSteps(this.verifyingSteps);
		return copy;
	}

	public RequirementVersionCoverage copyVerified(){
		return new RequirementVersionCoverage(this.verifiedRequirementVersion);
	}
	
	/**
	 * Will remove this entity from the verifying test case, the verified requirement and the verifying steps {@link RequirementVersionCoverage} lists.
	 */
	public void removeFromAll() {
		verifyingTestCase.removeRequirementVersionCoverage(this);
		verifiedRequirementVersion.removeRequirementVersionCoverage(this);
		for(ActionTestStep step : verifyingSteps){
			step.removeRequirementVersionCoverage(this);
		}		
	}
}
