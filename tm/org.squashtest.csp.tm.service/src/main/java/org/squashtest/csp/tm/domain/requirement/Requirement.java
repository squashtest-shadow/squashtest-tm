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

import static org.squashtest.csp.tm.domain.requirement.RequirementStatus.APPROVED;
import static org.squashtest.csp.tm.domain.requirement.RequirementStatus.OBSOLETE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.NoVerifiableRequirementVersionException;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.library.Library;

/**
 * Entity requirement
 * 
 * Note that much of its setters will throw an IllegalRequirementModificationException if a modification is attempted
 * while the status does not allow it.
 * 
 * @author bsiri
 * 
 */

@Entity
@PrimaryKeyJoinColumn(name = "RLN_ID")
public class Requirement extends RequirementLibraryNode<RequirementVersion> implements AttachmentHolder {
	/**
	 * The resource of this requirement is the latest version of the requirement.
	 */
	@OneToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "CURRENT_VERSION_ID")
	private RequirementVersion resource;

	@OneToMany(mappedBy = "requirement", cascade = { CascadeType.ALL })
	@OrderBy("versionNumber DESC")
	private List<RequirementVersion> versions = new ArrayList<RequirementVersion>();

	protected Requirement() {
		super();
	}

	/**
	 * Creates a new requirement which "latest version" is the given {@link RequirementVersion}
	 * 
	 * @param version
	 */
	public Requirement(@NotNull RequirementVersion version) {
		resource = version;
		addVersion(version);
	}

	private void addVersion(RequirementVersion version) {
		versions.add(version);
		version.setRequirement(this);
	}

	@Override
	public void setName(String name) {
		resource.setName(name);
	}

	@Override
	public void setDescription(String description) {
		resource.setDescription(description);
	}

	@Override
	public void accept(RequirementLibraryNodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public AttachmentList getAttachmentList() {
		return resource.getAttachmentList();
	}

	/***
	 * @return the reference of the requirement
	 */
	public String getReference() {
		return resource.getReference();
	}

	/***
	 * Set the requirement reference
	 * 
	 * @param reference
	 */
	public void setReference(String reference) {
		resource.setReference(reference);
	}

	/**
	 * Get the all the requirement versions numbers and status by the version Id
	 */
	public List<RequirementVersion> getRequirementVersions() {
		return Collections.unmodifiableList(versions);
	}

	/**
	 * Creates a copy usable in a copy / paste operation. The copy is associated to no version, it should be done by the
	 * caller (the latest version might not be eligible for copy / paste).
	 */
	@Override
	public Requirement createPastableCopy() {
		RequirementVersion latestVersionCopy = getCurrentVersion().createPastableCopy();
		Requirement copy = new Requirement(latestVersionCopy);

		for (RequirementVersion sourceVersion : this.versions) {
			if (isNotLatestVersion(sourceVersion) && sourceVersion.isNotObsolete()) {
				RequirementVersion copyVersion = sourceVersion.createPastableCopy();
				copy.addVersion(copyVersion);
			}
		}

		copy.notifyAssociatedWithProject(this.getProject());
		return copy;
	}

	private boolean isNotLatestVersion(RequirementVersion sourceVersion) {
		return !getCurrentVersion().equals(sourceVersion);
	}

	/***
	 * @return the requirement criticality
	 */
	public RequirementCriticality getCriticality() {
		return resource.getCriticality();
	}

	/***
	 * Set the requirement criticality
	 * 
	 * @param criticality
	 */
	public void setCriticality(RequirementCriticality criticality) {
		resource.setCriticality(criticality);
	}
	/***
	 * @return the requirement category
	 */
	public RequirementCategory getCategory() {
		return resource.getCategory();
	}

	/***
	 * Set the requirement category
	 * 
	 * @param category
	 */
	public void setCategory(RequirementCategory category) {
		resource.setCategory(category);
	}

	public void setStatus(RequirementStatus status) {
		resource.setStatus(status);
	}

	public RequirementStatus getStatus() {
		return resource.getStatus();
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

	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public String getDescription() {
		return resource.getDescription();
	}

	public RequirementVersion getCurrentVersion() {
		return resource;
	}

	@Override
	public RequirementVersion getResource() {
		return resource;
	}

	public void increaseVersion() {
		RequirementVersion previous = resource;
		RequirementVersion next = previous.createNextVersion();
		resource = next;
		versions.add(0, next);
		next.setRequirement(this);
	}
	
	public void increaseVersion(RequirementVersion newVersion) {
		newVersion.setVersionNumber(resource.getVersionNumber() + 1);
		resource = newVersion;
		versions.add(0, newVersion);
		newVersion.setRequirement(this);
		
	}

	/**
	 * returns this requirement's version which should be linked to a test case by default.
	 * 
	 * @return
	 */
	public RequirementVersion getDefaultVerifiableVersion() {
		RequirementVersion verifiable = findLatestApprovedVersion();

		if (verifiable == null) {
			verifiable = findLatestNonObsoleteVersion();
		}

		if (verifiable == null) {
			throw new NoVerifiableRequirementVersionException(this);
		}

		return verifiable;
	}

	private RequirementVersion findLatestApprovedVersion() {
		for (RequirementVersion version : versions) {
			if (APPROVED.equals(version.getStatus())) {
				return version;
			}
		}

		return null;
	}

	private RequirementVersion findLatestNonObsoleteVersion() {
		for (RequirementVersion version : versions) {
			if (version.isNotObsolete()) {
				return version;
			}
		}

		return null;
	}

	/**
	 * 
	 * @return an unmodifiable view of this requirement's versions
	 */
	public List<RequirementVersion> getUnmodifiableVersions() {
		return Collections.unmodifiableList(versions);
	}

	/**
	 * 
	 * @return false if all requirement versions are obsolete
	 */
	public boolean hasNonObsoleteVersion() {
		for (RequirementVersion version : this.versions) {
			if (!version.getStatus().equals(OBSOLETE)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return the last non obsolete requirement version <br>
	 *         or null if all versions are obsolete
	 */
	public RequirementVersion findLastNonObsoleteVersion() {
		for (RequirementVersion version : this.versions) {
			if (!version.getStatus().equals(OBSOLETE)) {
				return version;
			}
		}
		return null;
	}

	@Override
	public Library<?> getLibrary() {
		return getProject().getRequirementLibrary();
	}

	

}
