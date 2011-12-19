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

package org.squashtest.csp.tm.domain.requirement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.tm.domain.IllegalRequirementModificationException;
import org.squashtest.csp.tm.domain.RequirementNotLinkableException;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.testcase.TestCase;

/**
 * Represents a version of a requirement.
 * 
 * @author Gregory Fouquet
 * 
 */
public class RequirementVersion implements AttachmentHolder {
	@Id
	@GeneratedValue
	@Column(name = "REQ_VERSION_ID")
	private Long id;

	@NotBlank
	private String name;

	@Lob
	private String description;

	/**
	 * Collection of {@link Test Cases} verifying by this {@link Requirement}
	 */
	@NotNull
	@ManyToMany(mappedBy = "verifiedRequirements", cascade = { CascadeType.ALL })
	private final Set<TestCase> verifyingTestCases = new HashSet<TestCase>();

	/***
	 * The requirement reference
	 */
	private String reference;

	@NotNull
	@Enumerated(EnumType.STRING)
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "REQUIREMENT_STATUS")
	private RequirementStatus status = RequirementStatus.WORK_IN_PROGRESS;

	@NotNull
	@OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	@NotNull
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "REQUIREMENT_ID", updatable = false)
	private Requirement requirement;

	private int versionNumber = 1;

	public RequirementVersion() {
		super();
	}

	/**
	 * @see org.squashtest.csp.tm.domain.attachment.AttachmentHolder#getAttachmentList()
	 */
	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	public void setName(String name) {
		checkModifiable();
		this.name = name;
	}

	public void setDescription(String description) {
		checkModifiable();
		this.description = description;
	}

	/**
	 * Returns an UNMODIFIABLE VIEW of the verifying test cases.
	 */
	public Set<TestCase> getVerifyingTestCases() {
		return Collections.unmodifiableSet(verifyingTestCases);
	}

	public void addVerifyingTestCase(@NotNull TestCase testCase) throws RequirementNotLinkableException {
		testCase.addVerifiedRequirement(this);
	}

	public void removeVerifyingTestCase(@NotNull TestCase testCase) throws RequirementNotLinkableException {
		testCase.removeVerifiedRequirement(this);
	}

	private void checkLinkable() {
		if (!status.isRequirementLinkable()) {
			throw new RequirementNotLinkableException();
		}
	}

	/***
	 * @return the reference of the requirement
	 */
	public String getReference() {
		return reference;
	}

	/***
	 * Set the requirement reference
	 * 
	 * @param reference
	 */
	public void setReference(String reference) {
		checkModifiable();
		this.reference = reference;
	}

	/***
	 * @return the requirement criticality
	 */
	public RequirementCriticality getCriticality() {
		return criticality;
	}

	/***
	 * Set the requirement criticality
	 * 
	 * @param criticality
	 */
	public void setCriticality(RequirementCriticality criticality) {
		checkModifiable();
		this.criticality = criticality;
	}

	public void setStatus(RequirementStatus status) {
		checkStatusAccess(status);
		this.status = status;
	}

	public RequirementStatus getStatus() {
		return status;
	}

	private void checkModifiable() {
		if (!status.isRequirementModifiable()) {
			throw new IllegalRequirementModificationException();
		}
	}

	private void checkStatusAccess(RequirementStatus newStatus) {
		if ((!status.getAllowsStatusUpdate()) || (!status.isTransitionLegal(newStatus))) {
			throw new IllegalRequirementModificationException();
		}
	}

	/**
	 * 
	 * @return <code>true</code> if this requirement can be (un)linked by new verifying testcases
	 */
	public boolean isLinkable() {
		return getStatus().isRequirementLinkable();
	}

	/**
	 * Tells if this requirement's "intrinsic" properties can be modified. The following are not considered as
	 * "intrinsic" properties" : {@link #verifyingTestCases} are governed by the {@link #isLinkable()} state,
	 * {@link #status} is governed by itself.
	 * 
	 * @return <code>true</code> if this requirement's properties can be modified.
	 */
	public boolean isModifiable() {
		return getStatus().isRequirementModifiable();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	public void notifyVerifiedBy(@NotNull TestCase testCase) {
		checkLinkable();
		verifyingTestCases.add(testCase);

	}

	public void notifyNoLongerVerifiedBy(@NotNull TestCase testCase) {
		checkLinkable();
		verifyingTestCases.remove(testCase);

	}

	/**
	 * @return the requirement
	 */
	public Requirement getRequirement() {
		return requirement;
	}

	/**
	 * @return the versionNumber
	 */
	public int getVersionNumber() {
		return versionNumber;
	}

	/**
	 * Should be used once before this entity is persisted by the requirement to which this version is added.
	 * 
	 * @param requirement
	 */
	/* package-private */void setRequirement(Requirement requirement) {
		this.requirement = requirement;
	}

}
