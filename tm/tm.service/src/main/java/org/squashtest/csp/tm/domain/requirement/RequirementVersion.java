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
package org.squashtest.csp.tm.domain.requirement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.core.security.annotation.InheritsAcls;
import org.squashtest.csp.tm.domain.IllegalRequirementModificationException;
import org.squashtest.csp.tm.domain.RequirementAlreadyVerifiedException;
import org.squashtest.csp.tm.domain.RequirementVersionNotLinkableException;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.audit.AuditableMixin;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.resource.Resource;
import org.squashtest.csp.tm.domain.testcase.TestCase;

/**
 * Represents a version of a requirement.
 * 
 * @author Gregory Fouquet
 * 
 */
@Entity
@PrimaryKeyJoinColumn(name = "RES_ID")
@InheritsAcls(constrainedClass = Requirement.class, collectionName = "versions")
public class RequirementVersion extends Resource implements BoundEntity{
	/**
	 * Collection of {@link TestCase} verifying by this {@link Requirement}
	 */
	@NotNull
	@ManyToMany(mappedBy = "verifiedRequirementVersions")
	private final Set<TestCase> verifyingTestCases = new HashSet<TestCase>();

	/***
	 * The requirement reference. It should usually be set by the Requirement.
	 */
	private String reference;

	@NotNull
	@Enumerated(EnumType.STRING)
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;

	@NotNull
	@Enumerated(EnumType.STRING)
	private RequirementCategory category = RequirementCategory.UNDEFINED;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "REQUIREMENT_STATUS")
	private RequirementStatus status = RequirementStatus.WORK_IN_PROGRESS;

	

	@NotNull
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "REQUIREMENT_ID")
	private Requirement requirement;

	private int versionNumber = 1;

	public RequirementVersion() {
		super();
	}

	@Override
	public void setName(String name) {
		checkModifiable();
		super.setName(name);
	}

	@Override
	public void setDescription(String description) {
		checkModifiable();
		super.setDescription(description);
	}

	/**
	 * Returns an UNMODIFIABLE VIEW of the verifying test cases.
	 */
	public Set<TestCase> getVerifyingTestCases() {
		return Collections.unmodifiableSet(verifyingTestCases);
	}

	/**
	 * 
	 * @param testCase
	 * @throws RequirementVersionNotLinkableException
	 * @throws RequirementAlreadyVerifiedException
	 *             if another version of the same requirement is already verified by this test case.
	 */
	public void addVerifyingTestCase(@NotNull TestCase testCase) throws RequirementVersionNotLinkableException,
			RequirementAlreadyVerifiedException {
		testCase.addVerifiedRequirementVersion(this);
	}

	public void removeVerifyingTestCase(@NotNull TestCase testCase) throws RequirementVersionNotLinkableException {
		testCase.removeVerifiedRequirementVersion(this);
	}

	private void checkLinkable() {
		if (!status.isRequirementLinkable()) {
			throw new RequirementVersionNotLinkableException(this);
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

	/**
	 * @return the requirement category
	 */
	public RequirementCategory getCategory() {
		return category;
	}

	/***
	 * Set the requirement category
	 * 
	 * @param category
	 */
	public void setCategory(RequirementCategory category) {
		checkModifiable();
		this.category = category;
	}

	/**
	 * Sets this object's status, following status transition rules.
	 * 
	 * @param status
	 */
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

	public void notifyVerifiedBy(@NotNull TestCase testCase) {
		checkLinkable();
		verifyingTestCases.add(testCase);

	}

	public void notifyNoLongerVerifiedBy(@NotNull TestCase testCase) {
		// checkLinkable();
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

	protected void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	/**
	 * Should be used once before this entity is persisted by the requirement to which this version is added.
	 * 
	 * @param requirement
	 */
	/* package-private */void setRequirement(Requirement requirement) {
		this.requirement = requirement;
	}
	
	/**
	 * Will create a copy of the requirement version with all attributes, attachments and test-case associations.
	 * @return the requirement-version copy.
	 */
	public RequirementVersion createPastableCopy() {
		RequirementVersion copy = createBaselineCopy();
		copy.status = this.status;
		copy.versionNumber = this.versionNumber;
		copy.requirement = null;

		for (TestCase verifying : this.verifyingTestCases) {
			verifying.addCopyOfVerifiedRequirementVersion(copy);
		}

		attachCopiesOfAttachmentsTo(copy);

		return copy;
	}

	private void attachCopiesOfAttachmentsTo(RequirementVersion copy) {
		for (Attachment attachment : this.getAttachmentList().getAllAttachments()) {
			copy.getAttachmentList().addAttachment(attachment.hardCopy());
		}
	}

	private RequirementVersion createBaselineCopy() {
		RequirementVersion copy = new RequirementVersion();
		copy.setName(this.getName());
		copy.setDescription(this.getDescription());
		copy.criticality = this.criticality;
		copy.category = this.category;
		copy.reference = this.reference;
		return copy;
	}

	public boolean isNotObsolete() {
		return !RequirementStatus.OBSOLETE.equals(status);
	}

	/**
	 * Creates a {@link RequirementVersion} to be used as the one right after this RequirementVersion.
	 * 
	 * @return
	 */
	/* package-private */RequirementVersion createNextVersion() {
		RequirementVersion nextVersion = createBaselineCopy();
		nextVersion.status = RequirementStatus.WORK_IN_PROGRESS;
		nextVersion.versionNumber = this.versionNumber + 1;
		nextVersion.requirement = null;

		attachCopiesOfAttachmentsTo(nextVersion);

		return nextVersion;
	}

	/**
	 * Factory methiod which creates a {@link RequirementVersion} from a memento objet which holds the new object's target
	 * state. This method overrides any {@link RequirementStatus} workflow check.
	 * 
	 * @param memento
	 * @return
	 */
	public static RequirementVersion createFromMemento(@NotNull RequirementVersionImportMemento memento) {
		RequirementVersion res = new RequirementVersion();

		res.setName(memento.getName());
		res.setDescription(memento.getDescription());
		res.criticality = memento.getCriticality();
		res.category = memento.getCategory();
		res.reference = memento.getReference();
		res.status = memento.getStatus();

		AuditableMixin audit = ((AuditableMixin) res);

		audit.setCreatedOn(memento.getCreatedOn());
		audit.setCreatedBy(memento.getCreatedBy());

		return res;
	}
	
	// ***************** (detached) custom field section *************
	
	@Override
	public Long getBoundEntityId() {
		return getId();
	}
	
	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.REQUIREMENT_VERSION;
	}

	@Override
	public Project getProject() {
		return requirement.getProject();
	}
	
}
