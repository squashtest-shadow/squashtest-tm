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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;

/**
 * Entity requirement
 * 
 * Note that much of its setters will throw an
 * IllegalRequirementModificationException if a modification is attempted while
 * the status does not allow it.
 * 
 * @author bsiri
 * 
 */

@Entity
@PrimaryKeyJoinColumn(name = "RLN_ID")
public class Requirement extends RequirementLibraryNode implements
		AttachmentHolder {
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "LATEST_VERSION_ID")
	private RequirementVersion latestVersion;

	private Requirement() {
		super();
	}
	/**
	 * Creates a new requirement which "latest version" is the given {@link RequirementVersion}
	 * @param version 
	 */
	public Requirement(@NotNull RequirementVersion version) {
		latestVersion = version;
		latestVersion.setRequirement(this);
	}

	@Override
	public void setName(String name) {
		latestVersion.setName(name);
	}

	@Override
	public void setDescription(String description) {
		latestVersion.setDescription(description);
	}

	@Override
	public void accept(RequirementLibraryNodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public AttachmentList getAttachmentList() {
		return latestVersion.getAttachmentList();
	}

	/***
	 * @return the reference of the requirement
	 */
	public String getReference() {
		return latestVersion.getReference();
	}

	/***
	 * Set the requirement reference
	 * 
	 * @param reference
	 */
	public void setReference(String reference) {
		latestVersion.setReference(reference);
	}

	@Override
	public Requirement createCopy() {
		Requirement clone = new Requirement();
		clone.setStatus(RequirementStatus.WORK_IN_PROGRESS);

		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		clone.setReference(this.getReference());
		clone.setCriticality(this.getCriticality());
		// XXX RequirementVersion
		// for (TestCase testCase : this.verifyingTestCases) {
		// clone.addVerifyingTestCase(testCase);
		// }

		for (Attachment tcAttach : this.getAttachmentList().getAllAttachments()) {
			Attachment atCopy = tcAttach.hardCopy();
			clone.getAttachmentList().addAttachment(atCopy);
		}

		clone.notifyAssociatedWithProject(this.getProject());
		return clone;
	}

	/***
	 * @return the requirement criticality
	 */
	public RequirementCriticality getCriticality() {
		return latestVersion.getCriticality();
	}

	/***
	 * Set the requirement criticality
	 * 
	 * @param criticality
	 */
	public void setCriticality(RequirementCriticality criticality) {
		latestVersion.setCriticality(criticality);
	}

	public void setStatus(RequirementStatus status) {
		latestVersion.setStatus(status);
	}

	public RequirementStatus getStatus() {
		return latestVersion.getStatus();
	}

	/**
	 * 
	 * @return <code>true</code> if this requirement can be (un)linked by new
	 *         verifying testcases
	 */
	public boolean isLinkable() {
		return getStatus().isRequirementLinkable();
	}

	/**
	 * Tells if this requirement's "intrinsic" properties can be modified. The
	 * following are not considered as "intrinsic" properties" :
	 * {@link #verifyingTestCases} are governed by the {@link #isLinkable()}
	 * state, {@link #status} is governed by itself.
	 * 
	 * @return <code>true</code> if this requirement's properties can be
	 *         modified.
	 */
	public boolean isModifiable() {
		return getStatus().isRequirementModifiable();
	}
	@Override
	public String getName() {
		return latestVersion.getName();
	}
	@Override
	public String getDescription() {
		return latestVersion.getDescription();
	}

	public RequirementVersion getLatestVersion() {
		return latestVersion;
	}
}
